package ir.netpick.mailmine.scrape.model;

import ir.netpick.mailmine.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "api_key", indexes = {
        @Index(name = "idx_apikey_key", columnList = "key")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uniq_apikey_key", columnNames = { "key" })
})
public class ApiKey extends BaseEntity {

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "point_left")
    private Integer point;

    @Column(name = "link_id", nullable = false)
    private String apiLink;

    @Column(name = "search_engine_id", nullable = false)
    private String searchEngineId;

    public ApiKey() {
    }

    public ApiKey(String key, Integer point, String apiLink, String searchEngineId) {
        this.key = key;
        this.point = point;
        this.apiLink = apiLink;
        this.searchEngineId = searchEngineId;
    }

    public ApiKey(String key, Integer point, String apiLink, String searchEngineId, String description) {
        this.key = key;
        this.point = point;
        this.apiLink = apiLink;
        this.searchEngineId = searchEngineId;
        this.description = description;
    }

}
