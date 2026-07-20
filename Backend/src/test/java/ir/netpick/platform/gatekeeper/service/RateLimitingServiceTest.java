package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.AuthConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @Nested
    @DisplayName("canAttemptLogin Tests")
    class CanAttemptLoginTests {
        @Test
        @DisplayName("Should allow login when no attempts recorded")
        void shouldAllowLoginNoAttempts() {
            assertTrue(rateLimitingService.canAttemptLogin("user@test.com"));
        }

        @Test
        @DisplayName("Should allow login after lockout period")
        void shouldAllowLoginAfterLockout() {
            String email = "user@test.com";
            for (int i = 0; i < 5; i++) {
                rateLimitingService.recordFailedLoginAttempt(email);
            }
            rateLimitingService.clearLoginAttempts(email);

            assertTrue(rateLimitingService.canAttemptLogin(email));
        }
    }

    @Nested
    @DisplayName("recordFailedLoginAttempt Tests")
    class RecordFailedLoginAttemptTests {
        @Test
        @DisplayName("Should increment attempt count")
        void shouldIncrementAttemptCount() {
            String email = "user@test.com";
            rateLimitingService.recordFailedLoginAttempt(email);

            long remaining = rateLimitingService.getRemainingLockoutMinutes(email);
            assertEquals(0, remaining);
            assertTrue(rateLimitingService.canAttemptLogin(email));
        }
    }

    @Nested
    @DisplayName("getRemainingLockoutMinutes Tests")
    class GetRemainingLockoutMinutesTests {
        @Test
        @DisplayName("Should return 0 when no lockout")
        void shouldReturnZeroWhenNoLockout() {
            assertEquals(0, rateLimitingService.getRemainingLockoutMinutes("user@test.com"));
        }
    }

    @Nested
    @DisplayName("canAttemptVerification Tests")
    class CanAttemptVerificationTests {
        @Test
        @DisplayName("Should allow verification when no attempts recorded")
        void shouldAllowVerificationNoAttempts() {
            assertTrue(rateLimitingService.canAttemptVerification("user@test.com"));
        }

        @Test
        @DisplayName("Should deny verification after max attempts")
        void shouldDenyVerificationAfterMaxAttempts() {
            String email = "user@test.com";
            for (int i = 0; i < AuthConstants.VERIFICATION_MAX_ATTEMPTS + 1; i++) {
                rateLimitingService.recordVerificationAttempt(email);
            }

            assertFalse(rateLimitingService.canAttemptVerification(email));
        }
    }

    @Nested
    @DisplayName("recordVerificationAttempt Tests")
    class RecordVerificationAttemptTests {
        @Test
        @DisplayName("Should track multiple attempts")
        void shouldTrackMultipleAttempts() {
            String email = "user@test.com";
            for (int i = 0; i < AuthConstants.VERIFICATION_MAX_ATTEMPTS + 1; i++) {
                rateLimitingService.recordVerificationAttempt(email);
            }

            assertFalse(rateLimitingService.canAttemptVerification(email));
        }
    }

    @Nested
    @DisplayName("clearVerificationAttempts Tests")
    class ClearVerificationAttemptsTests {
        @Test
        @DisplayName("Should clear attempts and allow new verification")
        void shouldClearAttempts() {
            String email = "user@test.com";
            for (int i = 0; i < AuthConstants.VERIFICATION_MAX_ATTEMPTS + 1; i++) {
                rateLimitingService.recordVerificationAttempt(email);
            }

            rateLimitingService.clearVerificationAttempts(email);

            assertTrue(rateLimitingService.canAttemptVerification(email));
        }
    }

    @Nested
    @DisplayName("canResendVerification Tests")
    class CanResendVerificationTests {
        @Test
        @DisplayName("Should allow resend when no attempts recorded")
        void shouldAllowResendNoAttempts() {
            assertTrue(rateLimitingService.canResendVerification("user@test.com"));
        }

        @Test
        @DisplayName("Should deny resend after max per hour")
        void shouldDenyResendAfterMax() {
            String email = "user@test.com";
            for (int i = 0; i < 3; i++) {
                rateLimitingService.recordResendAttempt(email);
            }

            assertFalse(rateLimitingService.canResendVerification(email));
        }

        @Test
        @DisplayName("Should deny resend within minimum interval")
        void shouldDenyResendWithinInterval() {
            String email = "user@test.com";
            rateLimitingService.recordResendAttempt(email);

            assertFalse(rateLimitingService.canResendVerification(email));
        }
    }

    @Nested
    @DisplayName("clearResendAttempts Tests")
    class ClearResendAttemptsTests {
        @Test
        @DisplayName("Should clear resend attempts")
        void shouldClearResendAttempts() {
            String email = "user@test.com";
            rateLimitingService.recordResendAttempt(email);

            rateLimitingService.clearResendAttempts(email);

            assertTrue(rateLimitingService.canResendVerification(email));
        }
    }

    @Nested
    @DisplayName("clearLoginAttempts Tests")
    class ClearLoginAttemptsTests {
        @Test
        @DisplayName("Should clear login attempts")
        void shouldClearLoginAttempts() {
            String email = "user@test.com";
            for (int i = 0; i < 5; i++) {
                rateLimitingService.recordFailedLoginAttempt(email);
            }

            rateLimitingService.clearLoginAttempts(email);

            assertTrue(rateLimitingService.canAttemptLogin(email));
            assertEquals(0, rateLimitingService.getRemainingLockoutMinutes(email));
        }
    }

    @Test
    @DisplayName("Should provide correct resend limits")
    void shouldProvideCorrectResendLimits() {
        assertEquals(30, rateLimitingService.getResendMinSeconds());
        assertEquals(3, rateLimitingService.getResendMaxPerHour());
    }
}