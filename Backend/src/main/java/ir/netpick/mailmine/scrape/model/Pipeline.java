package ir.netpick.mailmine.scrape.model;

import ir.netpick.mailmine.common.BaseEntity;
import ir.netpick.mailmine.common.enums.PipelineStageEnum;
import ir.netpick.mailmine.common.enums.PipelineStateEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table
public class Pipeline extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private PipelineStageEnum stage;

    @Enumerated(EnumType.STRING)
    private PipelineStateEnum state;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    // Progress tracking
    @Column(name = "items_processed")
    private Integer itemsProcessed = 0;

    @Column(name = "items_total")
    private Integer itemsTotal = 0;

    @Column(name = "current_step_name")
    private String currentStepName;

    // Statistics
    @Column(name = "links_created")
    private Integer linksCreated = 0;

    @Column(name = "pages_scraped")
    private Integer pagesScraped = 0;

    @Column(name = "contacts_found")
    private Integer contactsFound = 0;

    @Column(name = "errors_count")
    private Integer errorsCount = 0;

    public Pipeline() {
    }

    public Pipeline(PipelineStageEnum stage, PipelineStateEnum state) {
        this.stage = stage;
        this.state = state;
    }

    public Pipeline(PipelineStageEnum stage, PipelineStateEnum state, LocalDateTime startTime) {
        this.stage = stage;
        this.state = state;
        this.startTime = startTime;
    }

    public Duration getDuration() {
        if (startTime == null)
            return null;
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end);
    }

    public String getDurationFormatted() {
        Duration d = getDuration();
        if (d == null)
            return "N/A";
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    public double getProgressPercent() {
        if (itemsTotal == null || itemsTotal == 0)
            return 0;
        return (itemsProcessed * 100.0) / itemsTotal;
    }

    public void incrementProcessed() {
        if (itemsProcessed == null)
            itemsProcessed = 0;
        itemsProcessed++;
    }

    public void incrementLinksCreated(int count) {
        if (linksCreated == null)
            linksCreated = 0;
        linksCreated += count;
    }

    public void incrementPagesScraped() {
        if (pagesScraped == null)
            pagesScraped = 0;
        pagesScraped++;
    }

    public void incrementContactsFound(int count) {
        if (contactsFound == null)
            contactsFound = 0;
        contactsFound += count;
    }

    public void incrementErrors() {
        if (errorsCount == null)
            errorsCount = 0;
        errorsCount++;
    }
}
