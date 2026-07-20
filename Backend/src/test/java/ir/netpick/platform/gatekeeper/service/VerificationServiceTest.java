package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.core.enums.RoleEnum;
import ir.netpick.platform.gatekeeper.model.Role;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.model.Verification;
import ir.netpick.platform.gatekeeper.repository.UserRepository;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerificationService verificationService;

    private UUID testUserId;
    private String testEmail;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "verify@test.com";

        Role userRole = new Role(RoleEnum.USER);
        userRole.setId(UUID.randomUUID());

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail(testEmail);
        testUser.setRole(userRole);
        testUser.setVerification(new Verification("123456"));
    }

    @Nested
    @DisplayName("prepareUserForVerification Tests")
    class PrepareVerificationTests {
        @Test
        @DisplayName("Should update verification code and save user")
        void shouldUpdateVerificationCode() {
            String code = verificationService.prepareUserForVerification(testUser);

            assertNotNull(code);
            assertEquals(8, code.length());
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("verifyCode Tests")
    class VerifyCodeTests {
        @Test
        @DisplayName("Should throw when user has no verification")
        void shouldThrowWhenNoVerification() {
            testUser.setVerification(null);

            assertThrows(ir.netpick.platform.core.exception.VerificationException.class, 
                    () -> verificationService.verifyCode(testUser, "123456"));
        }

        @Test
        @DisplayName("Should throw when code expired")
        void shouldThrowWhenCodeExpired() {
            Verification expiredVerification = new Verification("123456");
            expiredVerification.setVerificationExpiresAt(LocalDateTime.now().minusMinutes(10));
            testUser.setVerification(expiredVerification);

            assertThrows(ir.netpick.platform.core.exception.VerificationException.class, 
                    () -> verificationService.verifyCode(testUser, "123456"));
        }

        @Test
        @DisplayName("Should throw when max attempts reached")
        void shouldThrowWhenMaxAttemptsReached() {
            Verification verification = new Verification("123456");
            verification.setAttempts(5);
            testUser.setVerification(verification);

            assertThrows(ir.netpick.platform.core.exception.VerificationException.class, 
                    () -> verificationService.verifyCode(testUser, "wrong"));
        }

        @Test
        @DisplayName("Should throw when code does not match")
        void shouldThrowWhenCodeDoesNotMatch() {
            assertThrows(ir.netpick.platform.core.exception.VerificationException.class, 
                    () -> verificationService.verifyCode(testUser, "wrong"));
        }
    }
}