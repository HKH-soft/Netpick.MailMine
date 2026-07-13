package ir.netpick.mailmine.email.model;

import ir.netpick.mailmine.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "email_tags", indexes = {
    @Index(name = "idx_email_tags_name", columnList = "name"),
    @Index(name = "idx_email_tags_category", columnList = "category")
})
@Getter
@Setter
public class EmailTag extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagCategory category;

    @Column(name = "color_hex")
    private String colorHex;

    public enum TagCategory {
        SALES_LEAD,
        CUSTOMER,
        SUPPLIER,
        INVOICE,
        SUPPORT,
        HR,
        LEGAL,
        SPAM,
        NEWSLETTER,
        OTHER
    }
}