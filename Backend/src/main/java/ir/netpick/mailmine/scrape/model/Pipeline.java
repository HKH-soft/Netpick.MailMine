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

    @Column(name = "startTime")
    private LocalDateTime startTime;

    @Column(name = "endTime")
    private LocalDateTime endTime;

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
        if (startTime == null || endTime == null)
            return null;
        return Duration.between(startTime, endTime);
    }
}
