package ir.netpick.mailmine.scrape.service.mid;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import ir.netpick.mailmine.scrape.service.base.ScrapeDataService;
import ir.netpick.mailmine.scrape.service.base.ScrapeJobService;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ir.netpick.mailmine.scrape.ScrapeConstants;
import ir.netpick.mailmine.scrape.model.ScrapeJob;
import ir.netpick.mailmine.scrape.repository.ScrapeJobRepository;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class Scraper {

    @Value("${selenium.chrome.driver.path}")
    private String driverLocation;

    @Value("${selenium.chrome.binary.path}")
    private String chromeLocation;

    private final ScrapeJobRepository scrapeJobRepository;
    private final ScrapeJobService scrapeJobService;
    private final ScrapeDataService scrapeDataService;

    public int getDataCount() {
        return scrapeDataService.allData().size();
    }

    public void scrapePendingJobs() {
        scrapePendingJobs(true);
    }

    public void scrapePendingJobs(boolean headless) {
        List<ScrapeJob> scrapeJobs = fetchPendingJobs();

        if (scrapeJobs.isEmpty()) {
            log.error("No Scrape job left");
            return;
        }

        Optional<WebDriver> driverOpt = instantiateDriver(headless);
        if (driverOpt.isEmpty()) {
            log.error("Failed to instantiate WebDriver; aborting scrape.");
            return;
        }

        WebDriver driver = driverOpt.get();

        try {
            for (ScrapeJob scrapeJob : scrapeJobs) {
                processJob(scrapeJob, driver);
            }
        } finally {
            cleanupDriver(driver);
        }
    }

    private List<ScrapeJob> fetchPendingJobs() {
        return scrapeJobRepository.findByAttemptLessThanEqual(ScrapeConstants.MAX_ATTEMPTS);
    }

    private void processJob(ScrapeJob scrapeJob, WebDriver driver) {
        try {
            // Navigate to the URL
            driver.get(scrapeJob.getLink());

            // Explicit wait for page to load (e.g., body element visible)
            new WebDriverWait(driver, Duration.ofSeconds(ScrapeConstants.PAGE_LOAD_TIMEOUT_SECONDS))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

            String pageSource = driver.getPageSource();
            scrapeDataService.createScrapeData(pageSource, scrapeJob.getId());

            // Increment attempt only on success
            scrapeJob.setAttempt(scrapeJob.getAttempt() + 1);
            scrapeJobService.updateScrapeJob(scrapeJob.getId(), scrapeJob);

            // Clear cookies for next job to avoid session carryover
            driver.manage().deleteAllCookies();
        } catch (Exception e) {
            scrapeJob.setScrapeFailed(true);
            log.error("Failed to process scrape job {}: {}", scrapeJob.getId(), e.getMessage(), e);
        }
    }

    private Optional<WebDriver> instantiateDriver(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.setBinary(chromeLocation);

        System.setProperty("webdriver.chrome.driver", driverLocation);

        // Common arguments
        options.addArguments(
                "--disable-gpu",
                "--window-size=1920,1080",
                "--ignore-certificate-errors",
                "--disable-extensions",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0");

        if (headless) {
            options.addArguments("--headless");
        }


        try {
            return Optional.of(new ChromeDriver(options));
        } catch (SessionNotCreatedException e) {
            log.error("Driver encountered an issue during initialization: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private void cleanupDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                log.error("Error quitting WebDriver: {}", e.getMessage(), e);
            }
        }
    }
}