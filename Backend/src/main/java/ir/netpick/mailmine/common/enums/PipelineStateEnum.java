package ir.netpick.mailmine.common.enums;

public enum PipelineStateEnum {
    PENDING,
    RUNNING,
    PAUSED,
    CANCELLED,
    SKIPPING, // Skip current step and continue
    COMPLETED,
    FAILED;

    public boolean isFinished() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    public boolean isActive() {
        return this == RUNNING || this == PAUSED || this == SKIPPING;
    }

    public boolean shouldStop() {
        return this == CANCELLED || this == PAUSED;
    }
}
