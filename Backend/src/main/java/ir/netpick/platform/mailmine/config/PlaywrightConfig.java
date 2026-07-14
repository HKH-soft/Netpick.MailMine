package ir.netpick.platform.mailmine.config;

import com.microsoft.playwright.Playwright;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared Playwright instance configuration.
 * Reuses Playwright across scraping jobs for better resource efficiency.
 */
@Slf4j
@Configuration
public class PlaywrightConfig {

    private Playwright playwright;

    @Bean
    public Playwright playwright() {
        if (playwright == null) {
            playwright = Playwright.create();
            log.info("Created shared Playwright instance");
        }
        return playwright;
    }

    @PreDestroy
    public void cleanup() {
        if (playwright != null) {
            playwright.close();
            log.info("Closed shared Playwright instance");
        }
    }
}