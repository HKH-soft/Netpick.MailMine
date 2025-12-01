package ir.netpick.mailmine.scrape.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import ir.netpick.mailmine.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "scrape_data")
public class ScrapeData extends BaseEntity {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", referencedColumnName = "id", nullable = false)
    private ScrapeJob scrapeJob;

    @Column(name = "parsed")
    private boolean parsed;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ScrapeData(String fileName, int attemptNumber, ScrapeJob scrapeJob) {
        this.fileName = fileName;
        this.attemptNumber = attemptNumber;
        this.scrapeJob = scrapeJob;
    }

    public ScrapeData() {
    }

}
