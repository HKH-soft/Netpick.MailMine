package ir.netpick.mailmine.auth.service;

public interface RateLimiting {
    public boolean canAttemptLogin(String email);

    public void recordFailedLoginAttempt(String email);

    public void clearLoginAttempts(String email);

    public long getRemainingLockoutMinutes(String email);

    public boolean canAttemptVerification(String email);

    public void recordVerificationAttempt(String email);

    public boolean canResendVerification(String email);

    public int getResendMinSeconds();

    public int getResendMaxPerHour();

    public void recordResendAttempt(String email);

    public void clearVerificationAttempts(String email);

    public void clearResendAttempts(String email);
}
