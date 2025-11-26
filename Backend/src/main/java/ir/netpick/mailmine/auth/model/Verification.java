package ir.netpick.mailmine.auth.model;

import ir.netpick.mailmine.auth.AuthConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Embeddable
public class Verification {

    @Column(name = "verification_code")
    private String code;

    @Column(name = "verification_code_expires_at")
    private LocalDateTime verificationExpiresAt;

    @Column(name = "account_expires_at")
    private LocalDateTime accountExpiresAt;

    @Column(name = "verification_attempts")
    private Integer attempts;

    @Column(name = "verification_last_sent_at")
    private LocalDateTime lastSentAt;


    protected Verification() {
        // JPA only
    }

    public Verification(String code) {
        this.code = code;
        this.verificationExpiresAt = LocalDateTime.now().plusMinutes(AuthConstants.VERIFICATION_CODE_EXPIRATION_TIME_MIN);
        this.accountExpiresAt = LocalDateTime.now().plusHours(AuthConstants.VERIFICATION_ACCOUNT_EXPIRATION_TIME_HOUR);
        this.attempts = 0;
        this.lastSentAt = LocalDateTime.now();
    }

    public void updateCode(String newCode) {
        this.code = newCode;
        this.verificationExpiresAt = LocalDateTime.now().plusMinutes(AuthConstants.VERIFICATION_CODE_EXPIRATION_TIME_MIN); // Fixed to use minutes, not hours
        this.attempts = 0;
        this.lastSentAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(verificationExpiresAt);
    }

    public void incrementAttempts() {
        if (this.attempts == null) {
            this.attempts = 0;
        }
        this.attempts++;
    }

    public boolean hasReachedMaxAttempts() {
        return attempts != null && attempts >= AuthConstants.VERIFICATION_MAX_ATTEMPTS;
    }

    public boolean matches(String inputCode) {
        return code != null && code.equals(inputCode);
    }

    public void updateLastSent() {
        this.lastSentAt = LocalDateTime.now();
    }

    public boolean canResend(int cooldownSeconds) {
        return lastSentAt.plusSeconds(cooldownSeconds).isBefore(LocalDateTime.now());
    }
}