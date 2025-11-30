package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.AuthConstants;
import ir.netpick.mailmine.auth.exception.UserAlreadyVerifiedException;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.model.Verification;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.common.enums.RoleEnum;
import ir.netpick.mailmine.common.exception.VerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationService Unit Tests")
class VerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerificationService verificationService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName(RoleEnum.USER);

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setRole(userRole);
        testUser.setIsVerified(false);
    }

    @Nested
    @DisplayName("Verification Code Generation Tests")
    class VerificationCodeGenerationTests {

        @Test
        @DisplayName("Should generate code with correct length")
        void shouldGenerateCodeWithCorrectLength() {
            String code = verificationService.generateVerificationCode();

            int expectedLength = AuthConstants.VERIFICATION_CODE_LETTER_COUNT
                    + AuthConstants.VERIFICATION_CODE_DIGIT_COUNT;
            assertThat(code).hasSize(expectedLength);
        }

        @Test
        @DisplayName("Should generate code with letters and digits")
        void shouldGenerateCodeWithLettersAndDigits() {
            String code = verificationService.generateVerificationCode();

            // First part should be letters
            String letters = code.substring(0, AuthConstants.VERIFICATION_CODE_LETTER_COUNT);
            assertThat(letters).matches("[A-Z]+");

            // Second part should be digits
            String digits = code.substring(AuthConstants.VERIFICATION_CODE_LETTER_COUNT);
            assertThat(digits).matches("[0-9]+");
        }

        @Test
        @DisplayName("Should generate unique codes")
        void shouldGenerateUniqueCodes() {
            String code1 = verificationService.generateVerificationCode();
            String code2 = verificationService.generateVerificationCode();
            String code3 = verificationService.generateVerificationCode();

            // While not guaranteed, probability of collision is extremely low
            assertThat(code1).isNotEqualTo(code2);
            assertThat(code2).isNotEqualTo(code3);
        }
    }

    @Nested
    @DisplayName("Create Verification Tests")
    class CreateVerificationTests {

        @Test
        @DisplayName("Should create verification with generated code")
        void shouldCreateVerificationWithGeneratedCode() {
            Verification verification = verificationService.createVerification();

            assertThat(verification).isNotNull();
            assertThat(verification.getCode()).isNotNull();
            assertThat(verification.getCode()).hasSize(
                    AuthConstants.VERIFICATION_CODE_LETTER_COUNT + AuthConstants.VERIFICATION_CODE_DIGIT_COUNT);
        }

        @Test
        @DisplayName("Should create verification with zero attempts")
        void shouldCreateVerificationWithZeroAttempts() {
            Verification verification = verificationService.createVerification();

            assertThat(verification.getAttempts()).isZero();
        }

        @Test
        @DisplayName("Should create verification with future expiration")
        void shouldCreateVerificationWithFutureExpiration() {
            Verification verification = verificationService.createVerification();

            assertThat(verification.getVerificationExpiresAt()).isAfter(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("Verify Code Tests")
    class VerifyCodeTests {

        @Test
        @DisplayName("Should verify correct code successfully")
        void shouldVerifyCorrectCodeSuccessfully() {
            Verification verification = new Verification("ABCD1234");
            testUser.setVerification(verification);

            // Should not throw exception
            verificationService.verifyCode(testUser, "ABCD1234");
        }

        @Test
        @DisplayName("Should verify code case-insensitively")
        void shouldVerifyCodeCaseInsensitively() {
            Verification verification = new Verification("ABCD1234");
            testUser.setVerification(verification);

            // Should not throw - code is converted to uppercase
            verificationService.verifyCode(testUser, "abcd1234");
        }

        @Test
        @DisplayName("Should throw when verification is null")
        void shouldThrowWhenVerificationIsNull() {
            testUser.setVerification(null);

            assertThatThrownBy(() -> verificationService.verifyCode(testUser, "ABCD1234"))
                    .isInstanceOf(VerificationException.class)
                    .hasMessageContaining("No verification is currently pending");
        }

        @Test
        @DisplayName("Should throw when code is expired")
        void shouldThrowWhenCodeIsExpired() {
            Verification verification = new Verification("ABCD1234");
            // Set expiration to past
            verification.setVerificationExpiresAt(LocalDateTime.now().minusMinutes(1));
            testUser.setVerification(verification);

            assertThatThrownBy(() -> verificationService.verifyCode(testUser, "ABCD1234"))
                    .isInstanceOf(VerificationException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("Should throw when max attempts reached")
        void shouldThrowWhenMaxAttemptsReached() {
            Verification verification = new Verification("ABCD1234");
            verification.setAttempts(AuthConstants.VERIFICATION_MAX_ATTEMPTS);
            testUser.setVerification(verification);

            assertThatThrownBy(() -> verificationService.verifyCode(testUser, "ABCD1234"))
                    .isInstanceOf(VerificationException.class)
                    .hasMessageContaining("exceeded the maximum number");
        }

        @Test
        @DisplayName("Should throw and increment attempts on wrong code")
        void shouldThrowAndIncrementAttemptsOnWrongCode() {
            Verification verification = new Verification("ABCD1234");
            verification.setAttempts(0);
            testUser.setVerification(verification);

            assertThatThrownBy(() -> verificationService.verifyCode(testUser, "WRONG123"))
                    .isInstanceOf(VerificationException.class)
                    .hasMessageContaining("invalid");

            // Verify attempts were incremented
            assertThat(verification.getAttempts()).isEqualTo(1);

            // Verify user was saved
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should increment attempts on each wrong code")
        void shouldIncrementAttemptsOnEachWrongCode() {
            Verification verification = new Verification("ABCD1234");
            verification.setAttempts(0);
            testUser.setVerification(verification);

            // Try 3 wrong codes
            for (int i = 0; i < 3; i++) {
                try {
                    verificationService.verifyCode(testUser, "WRONG" + i);
                } catch (VerificationException e) {
                    // Expected
                }
            }

            assertThat(verification.getAttempts()).isEqualTo(3);
            verify(userRepository, times(3)).save(testUser);
        }
    }

    @Nested
    @DisplayName("Prepare User For Verification Tests")
    class PrepareUserForVerificationTests {

        @Test
        @DisplayName("Should throw when user is already verified")
        void shouldThrowWhenUserIsAlreadyVerified() {
            testUser.setIsVerified(true);

            assertThatThrownBy(() -> verificationService.prepareUserForVerification(testUser))
                    .isInstanceOf(UserAlreadyVerifiedException.class)
                    .hasMessageContaining("already verified");
        }

        @Test
        @DisplayName("Should auto-verify SUPER_ADMIN user")
        void shouldAutoVerifySuperAdminUser() {
            Role superAdminRole = new Role();
            superAdminRole.setName(RoleEnum.SUPER_ADMIN);
            testUser.setRole(superAdminRole);

            String result = verificationService.prepareUserForVerification(testUser);

            assertThat(result).isNull();
            assertThat(testUser.getIsVerified()).isTrue();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should create new verification for user without existing verification")
        void shouldCreateNewVerificationForUserWithoutExisting() {
            testUser.setVerification(null);

            String code = verificationService.prepareUserForVerification(testUser);

            assertThat(code).isNotNull();
            assertThat(testUser.getVerification()).isNotNull();
            assertThat(testUser.getVerification().getCode()).isEqualTo(code);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should update existing verification")
        void shouldUpdateExistingVerification() {
            Verification oldVerification = new Verification("OLDCODE1");
            testUser.setVerification(oldVerification);
            String oldCode = oldVerification.getCode();

            String newCode = verificationService.prepareUserForVerification(testUser);

            assertThat(newCode).isNotNull();
            assertThat(newCode).isNotEqualTo(oldCode);
            assertThat(testUser.getVerification().getAttempts()).isZero();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should reset attempts when updating verification")
        void shouldResetAttemptsWhenUpdatingVerification() {
            Verification verification = new Verification("OLDCODE1");
            verification.setAttempts(3);
            testUser.setVerification(verification);

            verificationService.prepareUserForVerification(testUser);

            assertThat(testUser.getVerification().getAttempts()).isZero();
        }
    }

    @Nested
    @DisplayName("Update Verification Tests")
    class UpdateVerificationTests {

        @Test
        @DisplayName("Should update code and reset attempts")
        void shouldUpdateCodeAndResetAttempts() {
            Verification verification = new Verification("OLDCODE1");
            verification.setAttempts(3);
            String oldCode = verification.getCode();

            verificationService.updateVerification(verification);

            assertThat(verification.getCode()).isNotEqualTo(oldCode);
            assertThat(verification.getAttempts()).isZero();
        }

        @Test
        @DisplayName("Should update expiration time")
        void shouldUpdateExpirationTime() {
            Verification verification = new Verification("OLDCODE1");
            LocalDateTime oldExpiration = verification.getVerificationExpiresAt();

            // Wait a tiny bit to ensure different timestamp
            verificationService.updateVerification(verification);

            assertThat(verification.getVerificationExpiresAt()).isAfterOrEqualTo(oldExpiration);
        }

        @Test
        @DisplayName("Should update last sent time")
        void shouldUpdateLastSentTime() {
            Verification verification = new Verification("OLDCODE1");
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            verificationService.updateVerification(verification);

            assertThat(verification.getLastSentAt()).isAfter(before);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle verification with null attempts")
        void shouldHandleVerificationWithNullAttempts() {
            Verification verification = new Verification("ABCD1234");
            verification.setAttempts(null);
            testUser.setVerification(verification);

            assertThatThrownBy(() -> verificationService.verifyCode(testUser, "WRONG123"))
                    .isInstanceOf(VerificationException.class);

            // Should have incremented from null to 1
            assertThat(verification.getAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle empty verification code input")
        void shouldHandleEmptyVerificationCodeInput() {
            Verification verification = new Verification("ABCD1234");
            testUser.setVerification(verification);

            assertThatThrownBy(() -> verificationService.verifyCode(testUser, ""))
                    .isInstanceOf(VerificationException.class)
                    .hasMessageContaining("invalid");
        }

        @Test
        @DisplayName("Should handle whitespace in verification code")
        void shouldHandleWhitespaceInVerificationCode() {
            Verification verification = new Verification("ABCD1234");
            testUser.setVerification(verification);

            // Code with spaces should fail
            assertThatThrownBy(() -> verificationService.verifyCode(testUser, " ABCD1234 "))
                    .isInstanceOf(VerificationException.class);
        }
    }
}
