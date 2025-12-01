package ir.netpick.mailmine.scrape.model;

import ir.netpick.mailmine.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "contacts")
public class Contact extends BaseEntity {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contact_emails", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "email")
    private Set<String> emails = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrape_data_id")
    private ScrapeData scrapeData;

    public Contact() {
    }

    public Contact(ScrapeData scrapeData) {
        this.scrapeData = scrapeData;
    }

    public boolean hasContactInfo() {
        return !(emails.isEmpty());
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", emails=" + emails +
                '}';
    }
}
