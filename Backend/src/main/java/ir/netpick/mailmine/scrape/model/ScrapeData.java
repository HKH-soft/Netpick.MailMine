package ir.netpick.mailmine.scrape.model;

import ir.netpick.mailmine.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "scrape_data")
public class ScrapeData extends BaseEntity {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @ManyToOne(cascade = CascadeType.REMOVE)
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
