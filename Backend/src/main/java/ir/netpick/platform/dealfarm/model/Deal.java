package ir.netpick.platform.dealfarm.model;

import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "deals")
public class Deal extends BaseEntity {

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private DealStage stage = DealStage.PROSPECTING;

    @Column(name = "value", precision = 19, scale = 2)
    private BigDecimal value;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "probability")
    private Integer probability;

    @Column(name = "expected_close_date")
    private LocalDateTime expectedCloseDate;

    public Deal() {
    }

    public boolean isClosed() {
        return stage == DealStage.CLOSED_WON || stage == DealStage.CLOSED_LOST;
    }

    public boolean isWon() {
        return stage == DealStage.CLOSED_WON;
    }
}