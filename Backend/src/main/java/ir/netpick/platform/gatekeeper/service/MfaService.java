package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.dto.MfaSetupResponse;
import ir.netpick.platform.gatekeeper.dto.MfaStatusResponse;
import ir.netpick.platform.gatekeeper.exception.InvalidTokenException;
import ir.netpick.platform.gatekeeper.model.BackupCode;
import ir.netpick.platform.gatekeeper.model.MfaSettings;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.BackupCodeRepository;
import ir.netpick.platform.gatekeeper.repository.MfaSettingsRepository;
import ir.netpick.platform.gatekeeper.repository.UserRepository;
import ir.netpick.platform.core.exception.RequestValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MfaService {

    private final MfaSettingsRepository mfaSettingsRepository;
    private final BackupCodeRepository backupCodeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityEventService securityEventService;

    @Value("${security.mfa.issuer:Netpick}")
    private String issuer;

    @Value("${security.mfa.backup-codes-count:10}")
    private int backupCodesCount;

    @Value("${security.mfa.totp-step:30}")
    private int totpStep;

    @Value("${security.mfa.totp-digits:6}")
    private int totpDigits;

    @Value("${security.mfa.totp-window:1}")
    private int totpWindow;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int TOTP_SECRET_LENGTH = 20;

    public MfaSetupResponse setupMfa(User user) {
        MfaSettings settings = mfaSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    MfaSettings s = new MfaSettings();
                    s.setUser(user);
                    return s;
                });

        if (settings.isMfaEnabled() && settings.isTotpVerified()) {
            throw new RequestValidationException("MFA is already enabled. Disable it first to reconfigure.");
        }

        String secret = generateBase32Secret();
        settings.setTotpSecret(secret);
        settings.setTotpVerified(false);
        mfaSettingsRepository.save(settings);

        String qrUrl = generateTotpUri(secret, user.getEmail());

        List<String> codes = generateBackupCodes();
        String codesDisplay = String.join("-", codes);

        log.info("MFA setup initiated for user: {}", user.getEmail());
        return new MfaSetupResponse(secret, qrUrl, codesDisplay);
    }

    @Transactional
    public void enableMfa(User user, String totpCode, String setupSecret) {
        MfaSettings settings = mfaSettingsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RequestValidationException("MFA setup not initiated. Call /mfa/setup first."));

        if (settings.isMfaEnabled() && settings.isTotpVerified()) {
            throw new RequestValidationException("MFA is already enabled.");
        }

        if (!settings.getTotpSecret().equals(setupSecret)) {
            throw new RequestValidationException("Invalid setup secret. Please restart MFA setup.");
        }

        if (!validateTotp(settings.getTotpSecret(), totpCode)) {
            throw new RequestValidationException("Invalid TOTP code. Please try again.");
        }

        settings.setTotpVerified(true);
        settings.setMfaEnabled(true);
        mfaSettingsRepository.save(settings);

        User userEntity = userRepository.findById(user.getId()).orElseThrow();
        userEntity.setMfaEnabled(true);
        userRepository.save(userEntity);

        storeBackupCodes(user);

        securityEventService.logEvent(
                ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.MFA_ENABLED,
                user.getId(), user.getEmail(), null, null, null,
                Map.of("method", "TOTP"), 0, false);

        log.info("MFA enabled for user: {}", user.getEmail());
    }

    @Transactional
    public void disableMfa(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RequestValidationException("Incorrect password. MFA was not disabled.");
        }

        MfaSettings settings = mfaSettingsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RequestValidationException("MFA is not configured."));

        if (!settings.isMfaEnabled()) {
            throw new RequestValidationException("MFA is not currently enabled.");
        }

        settings.setMfaEnabled(false);
        settings.setTotpVerified(false);
        settings.setTotpSecret(null);
        mfaSettingsRepository.save(settings);

        backupCodeRepository.deleteAllByUserId(user.getId());

        User userEntity = userRepository.findById(user.getId()).orElseThrow();
        userEntity.setMfaEnabled(false);
        userRepository.save(userEntity);

        securityEventService.logEvent(
                ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.MFA_DISABLED,
                user.getId(), user.getEmail(), null, null, null,
                Map.of(), 0, false);

        log.info("MFA disabled for user: {}", user.getEmail());
    }

    public boolean isMfaEnabled(UUID userId) {
        return mfaSettingsRepository.existsByUserIdAndMfaEnabledTrue(userId);
    }

    public boolean validateTotpCode(User user, String totpCode) {
        MfaSettings settings = mfaSettingsRepository.findByUserId(user.getId())
                .orElse(null);

        if (settings == null || !settings.isMfaEnabled() || !settings.isTotpVerified()) {
            // Fail closed - MFA validation should fail if settings are unavailable
            return false;
        }

        boolean valid = validateTotp(settings.getTotpSecret(), totpCode);
        if (valid) {
            settings.setLastUsedAt(LocalDateTime.now());
            mfaSettingsRepository.save(settings);
        }
        return valid;
    }

    public boolean validateBackupCode(User user, String backupCode) {
        List<BackupCode> codes = backupCodeRepository.findByUserIdAndUsedFalse(user.getId());

        for (BackupCode code : codes) {
            if (passwordEncoder.matches(backupCode, code.getCodeHash())) {
                code.setUsed(true);
                code.setUsedAt(LocalDateTime.now());
                backupCodeRepository.save(code);

                securityEventService.logEvent(
                        ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.MFA_BACKUP_CODE_USED,
                        user.getId(), user.getEmail(), null, null, null,
                        Map.of("remaining", backupCodeRepository.countByUserIdAndUsedFalse(user.getId())),
                        10, false);

                log.info("Backup code used for user: {}. Remaining: {}",
                        user.getEmail(), backupCodeRepository.countByUserIdAndUsedFalse(user.getId()));
                return true;
            }
        }

        log.warn("Invalid backup code attempt for user: {}", user.getEmail());
        return false;
    }

    public MfaStatusResponse getMfaStatus(User user) {
        MfaSettings settings = mfaSettingsRepository.findByUserId(user.getId()).orElse(null);

        if (settings == null || !settings.isMfaEnabled()) {
            return new MfaStatusResponse(false, false, 0, null);
        }

        long remaining = backupCodeRepository.countByUserIdAndUsedFalse(user.getId());
        return new MfaStatusResponse(
                settings.isMfaEnabled(),
                settings.isTotpVerified(),
                remaining,
                settings.getLastUsedAt());
    }

    private boolean validateTotp(String secret, String code) {
        if (code == null || code.length() != totpDigits) {
            return false;
        }

        byte[] secretBytes = base32Decode(secret);
        long currentEpoch = Instant.now().getEpochSecond();
        long timeStep = currentEpoch / totpStep;

        for (int i = -totpWindow; i <= totpWindow; i++) {
            String expectedCode = generateTotp(secretBytes, timeStep + i);
            // Use constant-time comparison to prevent timing attacks
            if (constantTimeEquals(expectedCode, code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Constant-time string comparison to prevent timing attacks on TOTP codes.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        byte[] aBytes = a.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] bBytes = b.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        try {
            return MessageDigest.isEqual(aBytes, bBytes);
        } catch (Exception e) {
            return false;
        }
    }

    private String generateTotp(byte[] secretBytes, long timeStep) {
        try {
            byte[] timeBytes = new byte[8];
            long value = timeStep;
            for (int i = 7; i >= 0; i--) {
                timeBytes[i] = (byte) (value & 0xFF);
                value >>= 8;
            }

            SecretKeySpec keySpec = new SecretKeySpec(secretBytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(timeBytes);

            int offset = hash[hash.length - 1] & 0x0F;
            int truncatedHash = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = truncatedHash % (int) Math.pow(10, totpDigits);
            return String.format("%0" + totpDigits + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("TOTP generation failed", e);
        }
    }

    private String generateBase32Secret() {
        byte[] bytes = new byte[TOTP_SECRET_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(BASE32_CHARS.charAt((b & 0xFF) % BASE32_CHARS.length()));
        }
        return sb.toString();
    }

    private byte[] base32Decode(String base32) {
        base32 = base32.replace("=", "").toUpperCase();
        int numBytes = base32.length() * 5 / 8;
        byte[] result = new byte[numBytes];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;

        for (char c : base32.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                result[index++] = (byte) (buffer >> bitsLeft);
            }
        }
        return Arrays.copyOf(result, index);
    }

    private String generateTotpUri(String secret, String email) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                issuer, email, secret, issuer, totpDigits, totpStep);
    }

    private List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < backupCodesCount; i++) {
            codes.add(generateRandomCode());
        }
        return codes;
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    @Transactional
    public void storeBackupCodes(User user) {
        backupCodeRepository.deleteAllByUserId(user.getId());

        List<String> codes = generateBackupCodes();
        List<BackupCode> backupCodes = codes.stream().map(code -> {
            BackupCode bc = new BackupCode();
            bc.setUser(user);
            bc.setCodeHash(passwordEncoder.encode(code));
            bc.setUsed(false);
            return bc;
        }).collect(Collectors.toList());

        backupCodeRepository.saveAll(backupCodes);
        log.info("Stored {} backup codes for user: {}", backupCodes.size(), user.getEmail());
    }
}
