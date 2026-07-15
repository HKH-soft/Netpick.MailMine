package ir.netpick.platform.financefarm.model;

import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Currency exchange rate entity.
 */
@Getter
@Setter
@Entity
@Table(name = "currency_rates")
public class CurrencyRate extends BaseEntity {

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @Column(name = "rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "source", length = 50)
    private String source = "TSE";

    public CurrencyRate() {
    }

    public CurrencyRate(String fromCurrency, String toCurrency, BigDecimal rate, LocalDate date) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.date = date;
    }
}