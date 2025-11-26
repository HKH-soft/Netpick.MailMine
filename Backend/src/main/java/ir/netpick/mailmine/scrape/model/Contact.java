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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contact_phones", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number")
    private Set<String> phoneNumbers = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contact_linkedin", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "linkedin_url")
    private Set<String> linkedInUrls = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contact_twitter", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "twitter_handle")
    private Set<String> twitterHandles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contact_github", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "github_profile")
    private Set<String> githubProfiles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contact_names", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "name")
    private Set<String> names = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrape_data_id")
    private ScrapeData scrapeData;

    public Contact() {
    }

    public Contact(ScrapeData scrapeData) {
        this.scrapeData = scrapeData;
    }

    public boolean hasContactInfo() {
        return !(emails.isEmpty() && phoneNumbers.isEmpty() &&
                linkedInUrls.isEmpty() && twitterHandles.isEmpty() &&
                githubProfiles.isEmpty() && names.isEmpty());
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", emails=" + emails +
                ", phones=" + phoneNumbers +
                ", linkedIn=" + linkedInUrls +
                ", twitter=" + twitterHandles +
                ", github=" + githubProfiles +
                ", names=" + names +
                '}';
    }
}
