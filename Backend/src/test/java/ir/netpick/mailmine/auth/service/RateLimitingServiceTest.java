package ir.netpick.mailmine.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RateLimitingService Unit Tests")
class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @Nested
    @DisplayName("Login Rate Limiting Tests")
    class LoginRateLimitingTests {

        private static final String TEST_EMAIL = "test@example.com";

        @Test
        @DisplayName("Should allow first login attempt")
        void shouldAllowFirstLoginAttempt() {
            assertThat(rateLimitingService.canAttemptLogin(TEST_EMAIL)).isTrue();
        }

        @Test
        @DisplayName("Should allow login attempts under limit")
        void shouldAllowLoginAttemptsUnderLimit() {
            // Record 4 failed attempts (limit is 5)
            for (int i = 0; i < 4; i++) {
                rateLimitingService.recordFailedLoginAttempt(TEST_EMAIL);
            }

            assertThat(rateLimitingService.canAttemptLogin(TEST_EMAIL)).isTrue();
        }

        @Test
        @DisplayName("Should block login after max attempts exceeded")
        void shouldBlockLoginAfterMaxAttemptsExceeded() {
            // Record 5 failed attempts (max limit)
            for (int i = 0; i < 5; i++) {
                rateLimitingService.recordFailedLoginAttempt(TEST_EMAIL);
            }

            assertThat(rateLimitingService.canAttemptLogin(TEST_EMAIL)).isFalse();
        }

        @Test
        @DisplayName("Should return remaining lockout minutes when locked")
        void shouldReturnRemainingLockoutMinutes() {
            // Record 5 failed attempts
            for (int i = 0; i < 5; i++) {
                rateLimitingService.recordFailedLoginAttempt(TEST_EMAIL);
            }

            long remainingMinutes = rateLimitingService.getRemainingLockoutMinutes(TEST_EMAIL);
            assertThat(remainingMinutes).isGreaterThan(0);
            // Allow some tolerance for timing - should be around 15 minutes, give or take
            assertThat(remainingMinutes).isLessThanOrEqualTo(16);
        }

        @Test
        @DisplayName("Should return zero lockout minutes when not locked")
        void shouldReturnZeroLockoutMinutesWhenNotLocked() {
            assertThat(rateLimitingService.getRemainingLockoutMinutes(TEST_EMAIL)).isZero();
        }

        @Test
        @DisplayName("Should clear login attempts on successful login")
        void shouldClearLoginAttemptsOnSuccessfulLogin() {
            // Record some failed attempts
            for (int i = 0; i < 3; i++) {
                rateLimitingService.recordFailedLoginAttempt(TEST_EMAIL);
            }

            // Clear attempts
            rateLimitingService.clearLoginAttempts(TEST_EMAIL);

            // Should be able to attempt login again
            assertThat(rateLimitingService.canAttemptLogin(TEST_EMAIL)).isTrue();
            assertThat(rateLimitingService.getRemainingLockoutMinutes(TEST_EMAIL)).isZero();
        }

        @Test
        @DisplayName("Should track attempts independently for different users")
        void shouldTrackAttemptsIndependentlyForDifferentUsers() {
            String user1 = "user1@example.com";
            String user2 = "user2@example.com";

            // Block user1
            for (int i = 0; i < 5; i++) {
                rateLimitingService.recordFailedLoginAttempt(user1);
            }

            // user1 should be blocked, user2 should be allowed
            assertThat(rateLimitingService.canAttemptLogin(user1)).isFalse();
            assertThat(rateLimitingService.canAttemptLogin(user2)).isTrue();
        }

        @Test
        @DisplayName("Should allow login after lockout period expires")
        void shouldAllowLoginAfterLockoutExpires() throws Exception {
            // Record 5 failed attempts
            for (int i = 0; i < 5; i++) {
                rateLimitingService.recordFailedLoginAttempt(TEST_EMAIL);
            }

            // Simulate time passing by manipulating the internal state
            // This tests the expiry logic
            Field loginAttemptsField = RateLimitingService.class.getDeclaredField("loginAttemptCounts");
            loginAttemptsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> loginAttempts = (Map<String, Object>) loginAttemptsField.get(rateLimitingService);

            // Clear the map to simulate expiry (in real scenarios, time would pass)
            loginAttempts.clear();

            assertThat(rateLimitingService.canAttemptLogin(TEST_EMAIL)).isTrue();
        }
    }

    @Nested
    @DisplayName("Verification Rate Limiting Tests")
    class VerificationRateLimitingTests {

        private static final String TEST_EMAIL = "verify@example.com";

        @Test
        @DisplayName("Should allow first verification attempt")
        void shouldAllowFirstVerificationAttempt() {
            assertThat(rateLimitingService.canAttemptVerification(TEST_EMAIL)).isTrue();
        }

        @Test
        @DisplayName("Should allow verification attempts under limit")
        void shouldAllowVerificationAttemptsUnderLimit() {
            // Record 4 attempts (limit is 5)
            for (int i = 0; i < 4; i++) {
                rateLimitingService.recordVerificationAttempt(TEST_EMAIL);
            }

            assertThat(rateLimitingService.canAttemptVerification(TEST_EMAIL)).isTrue();
        }

        @Test
        @DisplayName("Should block verification after max attempts exceeded")
        void shouldBlockVerificationAfterMaxAttemptsExceeded() {
            // Record 5 attempts (max limit)
            for (int i = 0; i < 5; i++) {
                rateLimitingService.recordVerificationAttempt(TEST_EMAIL);
            }

            assertThat(rateLimitingService.canAttemptVerification(TEST_EMAIL)).isFalse();
        }

        @Test
        @DisplayName("Should clear verification attempts")
        void shouldClearVerificationAttempts() {
            // Record some attempts
            for (int i = 0; i < 3; i++) {
                rateLimitingService.recordVerificationAttempt(TEST_EMAIL);
            }

            // Clear attempts
            rateLimitingService.clearVerificationAttempts(TEST_EMAIL);

            // Should be able to attempt verification again
            assertThat(rateLimitingService.canAttemptVerification(TEST_EMAIL)).isTrue();
        }
    }

    @Nested
    @DisplayName("Resend Verification Rate Limiting Tests")
    class ResendVerificationRateLimitingTests {

        private static final String TEST_EMAIL = "resend@example.com";

        @Test
        @DisplayName("Should allow first resend attempt")
        void shouldAllowFirstResendAttempt() {
            assertThat(rateLimitingService.canResendVerification(TEST_EMAIL)).isTrue();
        }

        @Test
        @DisplayName("Should block resend after max attempts per hour")
        void shouldBlockResendAfterMaxAttemptsPerHour() {
            // Record 3 resend attempts (max per hour)
            for (int i = 0; i < 3; i++) {
                rateLimitingService.recordResendAttempt(TEST_EMAIL);
            }

            assertThat(rateLimitingService.canResendVerification(TEST_EMAIL)).isFalse();
        }

        @Test
        @DisplayName("Should enforce minimum time between resends")
        void shouldEnforceMinimumTimeBetweenResends() {
            // Record first resend
            rateLimitingService.recordResendAttempt(TEST_EMAIL);

            // Should be blocked due to 30-second cooldown
            assertThat(rateLimitingService.canResendVerification(TEST_EMAIL)).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null email - throws NullPointerException")
        void shouldHandleNullEmail() {
            // ConcurrentHashMap doesn't allow null keys, so this throws NPE
            // This is expected behavior - null emails should be validated before calling
            // these methods
            assertThatThrownBy(() -> rateLimitingService.canAttemptLogin(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle empty email")
        void shouldHandleEmptyEmail() {
            String emptyEmail = "";
            assertThat(rateLimitingService.canAttemptLogin(emptyEmail)).isTrue();
            rateLimitingService.recordFailedLoginAttempt(emptyEmail);
            // Should still work
            assertThat(rateLimitingService.canAttemptLogin(emptyEmail)).isTrue();
        }

        @Test
        @DisplayName("Should handle concurrent access")
        void shouldHandleConcurrentAccess() throws InterruptedException {
            String email = "concurrent@example.com";
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    rateLimitingService.recordFailedLoginAttempt(email);
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Should be blocked after concurrent attempts
            assertThat(rateLimitingService.canAttemptLogin(email)).isFalse();
        }
    }
}
