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
@Table(name = "search_query", indexes = {
        @Index(name = "idx_searchquery_sentence", columnList = "sentence")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uniq_searchquery_sentence", columnNames = { "sentence" })
})
public class SearchQuery extends BaseEntity {

    @Column(name = "sentence", nullable = false)
    private String sentence;

    @Column(name = "link_count", nullable = false)
    private Integer linkCount;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SearchQuery() {
    }

    public SearchQuery(String sentence) {
        this.sentence = sentence;
        this.linkCount = 0;
    }

    public SearchQuery(String sentence, String description) {
        this.sentence = sentence;
        this.linkCount = 0;
        this.description = description;
    }

}
