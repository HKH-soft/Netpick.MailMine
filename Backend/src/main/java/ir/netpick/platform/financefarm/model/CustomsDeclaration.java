package ir.netpick.platform.financefarm.model;

import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Customs declaration for import/export shipments.
 */
@Getter
@Setter
@Entity
@Table(name = "customs_declarations")
public class CustomsDeclaration extends BaseEntity {

    @Column(name = "declaration_number", nullable = false, unique = true, length = 50)
    private String declarationNumber;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(name = "declaration_date", nullable = false)
    private LocalDate declarationDate;

    @Column(name = "customs_office", length = 100)
    private String customsOffice;

    @Column(name = "origin_country", length = 50)
    private String originCountry;

    @Column(name = "destination_country", length = 50)
    private String destinationCountry;

    @Column(name = "hs_code", length = 20)
    private String hsCode;

    @Column(name = "product_description", length = 500)
    private String productDescription;

    @Column(name = "quantity", precision = 19, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "customs_value", precision = 19, scale = 2)
    private BigDecimal customsValue;

    @Column(name = "duty_rate", precision = 10, scale = 4)
    private BigDecimal dutyRate;

    @Column(name = "duty_amount", precision = 19, scale = 2)
    private BigDecimal dutyAmount;

    @Column(name = "vat_rate", precision = 10, scale = 4)
    private BigDecimal vatRate = new BigDecimal("0.09");

    @Column(name = "vat_amount", precision = 19, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "total_tax", precision = 19, scale = 2)
    private BigDecimal totalTax;

    @Column(name = "status", length = 20)
    private String status = "DRAFT";

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @Column(name = "created_by")
    private UUID createdBy;

    public CustomsDeclaration() {
    }

    /**
     * Calculate duty amount based on customs value and duty rate.
     */
    public void calculateDuty() {
        if (customsValue != null && dutyRate != null) {
            this.dutyAmount = customsValue.multiply(dutyRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Calculate VAT amount based on customs value + duty.
     */
    public void calculateVat() {
        if (customsValue != null && vatRate != null) {
            BigDecimal base = customsValue.add(dutyAmount != null ? dutyAmount : BigDecimal.ZERO);
            this.vatAmount = base.multiply(vatRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Calculate total tax.
     */
    public void calculateTotalTax() {
        BigDecimal duty = dutyAmount != null ? dutyAmount : BigDecimal.ZERO;
        BigDecimal vat = vatAmount != null ? vatAmount : BigDecimal.ZERO;
        this.totalTax = duty.add(vat).setScale(2, RoundingMode.HALF_UP);
    }
}