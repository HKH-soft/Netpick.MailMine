package ir.netpick.platform.gatekeeper.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import ir.netpick.platform.gatekeeper.dto.*;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.service.DeviceSessionService;
import ir.netpick.platform.gatekeeper.service.MfaService;
import ir.netpick.platform.core.exception.RequestValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gatekeeper/mfa")
@RequiredArgsConstructor
@Tag(name = "MFA", description = "Multi-Factor Authentication endpoints")
public class MfaController {

    private final MfaService mfaService;
    private final DeviceSessionService deviceSessionService;

    @Operation(summary = "Get MFA status", description = "Check current MFA configuration status for the authenticated user")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/status")
    public ResponseEntity<MfaStatusResponse> getMfaStatus(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mfaService.getMfaStatus(user));
    }

    @Operation(summary = "Setup MFA", description = "Initiate MFA setup. Returns TOTP secret, QR code URL, and backup codes.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/setup")
    public ResponseEntity<MfaSetupResponse> setupMfa(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mfaService.setupMfa(user));
    }

    @Operation(summary = "Enable MFA", description = "Enable MFA after verifying TOTP code from authenticator app")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/enable")
    public ResponseEntity<MessageResponse> enableMfa(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MfaEnableRequest request) {
        mfaService.enableMfa(user, request.totpCode(), request.setupSecret());
        return ResponseEntity.ok(new MessageResponse("MFA enabled successfully"));
    }

    @Operation(summary = "Verify MFA code", description = "Verify a TOTP code during authentication (step-up auth)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/verify")
    public ResponseEntity<MessageResponse> verifyMfa(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MfaVerifyRequest request) {
        boolean valid = mfaService.validateTotpCode(user, request.totpCode());
        if (!valid) {
            throw new RequestValidationException("Invalid TOTP code");
        }
        return ResponseEntity.ok(new MessageResponse("TOTP code verified successfully"));
    }

    @Operation(summary = "Verify backup code", description = "Use a backup code for MFA verification")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/verify-backup")
    public ResponseEntity<MessageResponse> verifyBackupCode(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MfaBackupCodeRequest request) {
        boolean valid = mfaService.validateBackupCode(user, request.backupCode());
        if (!valid) {
            throw new RequestValidationException("Invalid or already used backup code");
        }
        return ResponseEntity.ok(new MessageResponse("Backup code verified successfully"));
    }

    @Operation(summary = "Disable MFA", description = "Disable MFA. Requires password confirmation.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/disable")
    public ResponseEntity<MessageResponse> disableMfa(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MfaDisableRequest request) {
        mfaService.disableMfa(user, request.password());
        return ResponseEntity.ok(new MessageResponse("MFA disabled successfully"));
    }

    @Operation(summary = "Refresh backup codes", description = "Generate new backup codes (old ones will be invalidated)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/backup-codes/refresh")
    public ResponseEntity<MessageResponse> refreshBackupCodes(@AuthenticationPrincipal User user) {
        mfaService.storeBackupCodes(user);
        return ResponseEntity.ok(new MessageResponse("Backup codes refreshed successfully"));
    }

    @Operation(summary = "Get active sessions", description = "List all active device sessions for the authenticated user")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/sessions")
    public ResponseEntity<List<DeviceSessionDTO>> getActiveSessions(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) java.util.UUID currentSessionId) {
        return ResponseEntity.ok(deviceSessionService.getActiveSessions(user.getId(), currentSessionId));
    }

    @Operation(summary = "Revoke a session", description = "Revoke a specific device session")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<MessageResponse> revokeSession(
            @AuthenticationPrincipal User user,
            @PathVariable java.util.UUID sessionId) {
        deviceSessionService.revokeSession(user.getId(), sessionId, user.getEmail());
        return ResponseEntity.ok(new MessageResponse("Session revoked successfully"));
    }

    @Operation(summary = "Revoke all sessions", description = "Revoke all active sessions (forces re-login on all devices)")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/sessions")
    public ResponseEntity<MessageResponse> revokeAllSessions(@AuthenticationPrincipal User user) {
        deviceSessionService.revokeAllSessions(user.getId());
        return ResponseEntity.ok(new MessageResponse("All sessions revoked successfully"));
    }
}
