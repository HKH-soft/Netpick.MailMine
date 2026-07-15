package ir.netpick.platform.financefarm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for fetching exchange rates from Tehran Stock Exchange (TSE).
 * Provides daily IRR/USD/TMN rates.
 */
@Service
@Slf4j
public class TseExchangeRateService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tse.api.url:https://api.tgju.org/v1/}")
    private String tseApiUrl;

    @Value("${tse.api.key:}")
    private String tseApiKey;

    /**
     * Get USD to IRR exchange rate.
     */
    @Cacheable(value = "exchangeRates", key = "'USD_IRR'")
    public BigDecimal getUsdToIrrRate() {
        return fetchRate("USD_IRR");
    }

    /**
     * Get EUR to IRR exchange rate.
     */
    @Cacheable(value = "exchangeRates", key = "'EUR_IRR'")
    public BigDecimal getEurToIrrRate() {
        return fetchRate("EUR_IRR");
    }

    /**
     * Get all available exchange rates.
     */
    @Cacheable(value = "exchangeRates", key = "'ALL_RATES'")
    public Map<String, BigDecimal> getAllRates() {
        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("USD_IRR", getUsdToIrrRate());
        rates.put("EUR_IRR", getEurToIrrRate());
        rates.put("TRY_IRR", fetchRate("TRY_IRR"));
        rates.put("CNY_IRR", fetchRate("CNY_IRR"));
        return rates;
    }

    /**
     * Fetch exchange rate from TSE API.
     */
    private BigDecimal fetchRate(String pair) {
        try {
            String url = tseApiUrl + "currency/" + pair + "?api_key=" + tseApiKey;
            ResponseEntity<TseResponse> response = restTemplate.getForEntity(url, TseResponse.class);
            
            if (response.getBody() != null && response.getBody().getData() != null) {
                return new BigDecimal(response.getBody().getData().getRate());
            }
        } catch (Exception e) {
            log.error("Failed to fetch exchange rate for {}: {}", pair, e.getMessage());
            // Return fallback rates
            return getFallbackRate(pair);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Fallback rates when API unavailable.
     */
    private BigDecimal getFallbackRate(String pair) {
        return switch (pair) {
            case "USD_IRR" -> new BigDecimal("420000");
            case "EUR_IRR" -> new BigDecimal("450000");
            case "TRY_IRR" -> new BigDecimal("22000");
            case "CNY_IRR" -> new BigDecimal("58000");
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * Update rates daily at 9 AM Iran time.
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @CacheEvict(value = "exchangeRates", allEntries = true)
    public void updateRates() {
        log.info("Updating exchange rates from TSE");
        getAllRates();
    }

    /**
     * Convert amount from one currency to another.
     */
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        BigDecimal rate = switch (fromCurrency + "_" + toCurrency) {
            case "USD_IRR" -> getUsdToIrrRate();
            case "IRR_USD" -> BigDecimal.ONE.divide(getUsdToIrrRate(), 6, RoundingMode.HALF_UP);
            case "EUR_IRR" -> getEurToIrrRate();
            case "IRR_EUR" -> BigDecimal.ONE.divide(getEurToIrrRate(), 6, RoundingMode.HALF_UP);
            default -> throw new IllegalArgumentException("Unsupported currency pair: " + fromCurrency + "_" + toCurrency);
        };

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    // Inner classes for API response
    @lombok.Data
    private static class TseResponse {
        private Data data;
    }

    @lombok.Data
    private static class Data {
        private String rate;
    }
}