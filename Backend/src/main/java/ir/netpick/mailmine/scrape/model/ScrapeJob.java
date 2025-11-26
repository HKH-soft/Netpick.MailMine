package ir.netpick.mailmine.scrape.model;

import ir.netpick.mailmine.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "scrape_job", indexes = {
        @Index(name = "idx_scrapejob_link", columnList = "scrape_link")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uniq_scrapejob_link", columnNames = { "scrape_link" })
})
public class ScrapeJob extends BaseEntity {

    @Column(name = "scrape_link", nullable = false)
    private String link;

    @Column(name = "attempt_number", nullable = false)
    private Integer attempt = 0;

    // @ManyToOne(cascade = CascadeType.REMOVE)
    // @JoinColumn(name = "query", referencedColumnName = "id", nullable = false)
    // private SearchQuery query;

    @Column(name = "been_scraped")
    private Boolean beenScraped = false;

    @Column(name = "scrape_failed")
    private Boolean scrapeFailed = false;

    public ScrapeJob() {
    }

    public ScrapeJob(String link) {
        this.link = link;
    }

    public ScrapeJob(String link, String description) {
        this.link = link;
        this.description = description;
    }
}
