package ir.netpick.platform.gatekeeper.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import ir.netpick.platform.gatekeeper.dto.*;
import ir.netpick.platform.gatekeeper.model.IpPolicy;
import ir.netpick.platform.gatekeeper.model.SecurityEvent;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.service.DeviceSessionService;
import ir.netpick.platform.gatekeeper.service.IpPolicyService;
import ir.netpick.platform.gatekeeper.service.SecurityEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gatekeeper/admin/security")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Security Administration", description = "Admin endpoints for security monitoring and management")
public class SecurityAdminController {

    private final SecurityEventService securityEventService;
    private final IpPolicyService ipPolicyService;
    private final DeviceSessionService deviceSessionService;

    @Operation(summary = "Security dashboard", description = "Get overview of security events, threats, and anomalies")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/dashboard")
    public ResponseEntity<SecurityDashboardDTO> getDashboard() {
        return ResponseEntity.ok(securityEventService.getDashboard());
    }

    @Operation(summary = "Get security events", description = "List security events with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/events")
    public ResponseEntity<Page<SecurityEvent>> getEvents(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(securityEventService.getRecentEvents(hours, page, size));
    }

    @Operation(summary = "Get user security events", description = "List security events for a specific user")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/events/user/{userId}")
    public ResponseEntity<Page<SecurityEvent>> getUserEvents(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(securityEventService.getEventsByUser(userId, page, size));
    }

    @Operation(summary = "Get high-risk events", description = "List events with risk score above threshold")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/events/high-risk")
    public ResponseEntity<Page<SecurityEvent>> getHighRiskEvents(
            @RequestParam(defaultValue = "50") int minRisk,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(securityEventService.getHighRiskEvents(minRisk, page, size));
    }

    @Operation(summary = "Create IP policy", description = "Create an IP allowlist or blocklist policy")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/ip-policies")
    public ResponseEntity<IpPolicyDTO> createIpPolicy(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody IpPolicyCreateRequest request) {
        return ResponseEntity.ok(ipPolicyService.createPolicy(request, user));
    }

    @Operation(summary = "List IP policies", description = "Get all IP access policies")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/ip-policies")
    public ResponseEntity<List<IpPolicyDTO>> getIpPolicies(
            @RequestParam(required = false) String type) {
        if (type != null) {
            return ResponseEntity.ok(ipPolicyService.getPoliciesByType(type));
        }
        return ResponseEntity.ok(ipPolicyService.getAllPolicies());
    }

    @Operation(summary = "Delete IP policy", description = "Soft-delete an IP access policy")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/ip-policies/{policyId}")
    public ResponseEntity<MessageResponse> deleteIpPolicy(
            @AuthenticationPrincipal User user,
            @PathVariable UUID policyId) {
        ipPolicyService.deletePolicy(policyId, user);
        return ResponseEntity.ok(new MessageResponse("IP policy deleted successfully"));
    }

    @Operation(summary = "Get user sessions", description = "List active sessions for a specific user (admin view)")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/users/{userId}/sessions")
    public ResponseEntity<List<DeviceSessionDTO>> getUserSessions(@PathVariable UUID userId) {
        return ResponseEntity.ok(deviceSessionService.getActiveSessions(userId, null));
    }

    @Operation(summary = "Revoke user session", description = "Revoke a specific session for a user (admin action)")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/users/{userId}/sessions/{sessionId}")
    public ResponseEntity<MessageResponse> revokeUserSession(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID userId,
            @PathVariable UUID sessionId) {
        deviceSessionService.revokeSession(userId, sessionId, admin.getEmail());
        return ResponseEntity.ok(new MessageResponse("Session revoked successfully"));
    }

    @Operation(summary = "Revoke all user sessions", description = "Revoke all sessions for a specific user (admin action)")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/users/{userId}/sessions")
    public ResponseEntity<MessageResponse> revokeAllUserSessions(@PathVariable UUID userId) {
        deviceSessionService.revokeAllSessions(userId);
        return ResponseEntity.ok(new MessageResponse("All user sessions revoked successfully"));
    }
}
