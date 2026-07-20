package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.core.exception.RequestValidationException;
import ir.netpick.platform.gatekeeper.dto.MfaSetupResponse;
import ir.netpick.platform.gatekeeper.dto.MfaStatusResponse;
import ir.netpick.platform.gatekeeper.model.BackupCode;
import ir.netpick.platform.gatekeeper.model.MfaSettings;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.BackupCodeRepository;
import ir.netpick.platform.gatekeeper.repository.MfaSettingsRepository;
import ir.netpick.platform.gatekeeper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock
    private MfaSettingsRepository mfaSettingsRepository;

    @Mock
    private BackupCodeRepository backupCodeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityEventService securityEventService;

    @InjectMocks
    private MfaService mfaService;

    private UUID testUserId;
    private String testEmail;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "mfa@test.com";

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail(testEmail);
        testUser.setPasswordHash("encodedPassword");
        testUser.setMfaEnabled(false);

        setField(mfaService, "backupCodesCount", 10);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("setupMfa Tests")
    class SetupMfaTests {
        @Test
        @DisplayName("Should create MFA settings and return setup response")
        void shouldSetupMfa() {
            when(mfaSettingsRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(mfaSettingsRepository.save(any(MfaSettings.class))).thenAnswer(inv -> inv.getArgument(0));

            MfaSetupResponse response = mfaService.setupMfa(testUser);

            assertNotNull(response);
            assertNotNull(response.secret());
            assertNotNull(response.qrCodeUrl());
            assertTrue(response.backupCodes().contains("-"));
        }

        @Test
        @DisplayName("Should throw when MFA already fully enabled")
        void shouldThrowWhenMfaAlreadyEnabled() {
            MfaSettings existingSettings = new MfaSettings();
            existingSettings.setMfaEnabled(true);
            existingSettings.setTotpVerified(true);

            when(mfaSettingsRepository.findByUserId(testUserId)).thenReturn(Optional.of(existingSettings));

            assertThrows(RequestValidationException.class, () -> mfaService.setupMfa(testUser));
        }
    }

    @Nested
    @DisplayName("enableMfa Tests")
    class EnableMfaTests {
        @Test
        @DisplayName("Should throw when MFA setup not initiated")
        void shouldThrowWhenSetupNotInitiated() {
            when(mfaSettingsRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

            assertThrows(RequestValidationException.class, 
                    () -> mfaService.enableMfa(testUser, "123456", "secret"));
        }

        @Test
        @DisplayName("Should throw when setup secret mismatch")
        void shouldThrowWhenSecretMismatch() {
            MfaSettings settings = new MfaSettings();
            settings.setTotpSecret("correct-secret");

            when(mfaSettingsRepository.findByUserId(testUserId)).thenReturn(Optional.of(settings));

            assertThrows(RequestValidationException.class, 
                    () -> mfaService.enableMfa(testUser, "123456", "wrong-secret"));
        }

        @Test
        @DisplayName("Should throw when TOTP code invalid")
        void shouldThrowWhenTotpInvalid() {
            MfaSettings settings = new MfaSettings();
            settings.setTotpSecret("JBSWY3DPEHPK3PXP"); // Standard test secret

            when(mfaSettingsRepository.findByUserId(testUserId)).thenReturn(Optional.of(settings));

            assertThrows(RequestValidationException.class, 
                    () -> mfaService.enableMfa(testUser, "000000", "JBSWY3DPEHPK3PXP"));
        }
    }

    @Nested
    @DisplayName("disableMfa Tests")
    class DisableMfaTests {
        @Test
        @DisplayName("Should throw when password incorrect")
        void shouldThrowWhenPasswordIncorrect() {
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThrows(RequestValidationException.class, 
                    () -> mfaService.disableMfa(testUser, "wrong-password"));
        }

        @Test
        @DisplayName("Should throw when MFA not configured")
        void shouldThrowWhenMfaNotConfigured() {
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(mfaSettingsRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

            assertThrows(RequestValidationException.class, 
                    () -> mfaService.disableMfa(testUser, "correct-password"));
        }

        @Test
        @DisplayName("Should throw when MFA not enabled")
        void shouldThrowWhenMfaNotEnabled() {
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            MfaSettings settings = new MfaSettings();
            settings.setMfaEnabled(false);

            when(mfaSettingsRepository.findByUserId(testUserId)).thenReturn(Optional.of(settings));

            assertThrows(RequestValidationException.class, 
                    () -> mfaService.disableMfa(testUser, "correct-password"));
        }
    }

    @Nested
    @DisplayName("isMfaEnabled Tests")
    class IsMfaEnabledTests {
        @Test
        @DisplayName("Should return true when enabled")
        void shouldReturnTrueWhenEnabled() {
            when(mfaSettingsRepository.existsByUserIdAndMfaEnabledTrue(testUserId)).thenReturn(true);

            assertTrue(mfaService.isMfaEnabled(testUserId));
        }

        @Test
        @DisplayName("Should return false when not enabled")
        void shouldReturnFalseWhenNotEnabled() {
            when(mfaSettingsRepository.existsByUserIdAndMfaEnabledTrue(testUserId)).thenReturn(false);

            assertFalse(mfaService.isMfaEnabled(testUserId));
        }
    }

    @Nested
    @DisplayName("validateBackupCode Tests")
    class ValidateBackupCodeTests {
        @Test
        @DisplayName("Should return false when no backup codes exist")
        void shouldReturnFalseWhenNoCodes() {
            when(backupCodeRepository.findByUserIdAndUsedFalse(testUserId)).thenReturn(List.of());

            assertFalse(mfaService.validateBackupCode(testUser, "12345678"));
        }

        @Test
        @DisplayName("Should return true and mark code as used when valid")
        void shouldValidateAndUseBackupCode() {
            BackupCode validCode = new BackupCode();
            validCode.setCodeHash("encoded-code");
            validCode.setUsed(false);

            when(backupCodeRepository.findByUserIdAndUsedFalse(testUserId)).thenReturn(List.of(validCode));
            when(passwordEncoder.matches("12345678", "encoded-code")).thenReturn(true);

            boolean result = mfaService.validateBackupCode(testUser, "12345678");

            assertTrue(result);
            verify(backupCodeRepository).save(any(BackupCode.class));
        }
    }

    @Nested
    @DisplayName("getMfaStatus Tests")
    class GetMfaStatusTests {
        @Test
        @DisplayName("Should return disabled status when no settings")
        void shouldReturnDisabledStatus() {
            when(mfaSettingsRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

            MfaStatusResponse response = mfaService.getMfaStatus(testUser);

            assertNotNull(response);
            assertFalse(response.mfaEnabled());
            assertFalse(response.totpVerified());
            assertEquals(0, response.backupCodesRemaining());
        }
    }
}