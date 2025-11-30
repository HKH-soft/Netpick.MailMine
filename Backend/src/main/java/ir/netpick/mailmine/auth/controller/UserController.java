package ir.netpick.mailmine.auth.controller;

import ir.netpick.mailmine.auth.dto.UserDTO;
import ir.netpick.mailmine.auth.dto.UserUpdateRequest;
import ir.netpick.mailmine.auth.email.AuthEmailService;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.service.UserService;
import ir.netpick.mailmine.common.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthEmailService authEmailService;

    // ==================== Current User Operations ====================

    /**
     * Get current authenticated user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUser(user.getEmail()));
    }

    /**
     * Update current authenticated user's profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(
            @AuthenticationPrincipal User user,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(user.getEmail(), request));
    }

    /**
     * Change current user's password
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal User user,
            @RequestBody PasswordChangeRequest request) {
        userService.changePassword(user.getEmail(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Delete current user's account (soft delete)
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal User user) {
        userService.deleteUser(user.getEmail());
        return ResponseEntity.noContent().build();
    }

    // ==================== Admin Operations ====================

    /**
     * Get all users (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PageDTO<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(userService.allUsers(page));
    }

    /**
     * Get a specific user by ID (admin only)
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDTO> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    /**
     * Update a user by ID (admin only)
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID userId,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    /**
     * Soft delete a user (admin only)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restore a soft-deleted user (admin only)
     */
    @PostMapping("/{userId}/restore")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> restoreUser(@PathVariable UUID userId) {
        userService.restoreUser(userId);
        return ResponseEntity.ok(Map.of("message", "User restored successfully"));
    }

    /**
     * Permanently delete a user (super admin only)
     */
    @DeleteMapping("/{userId}/permanent")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> permanentlyDeleteUser(@PathVariable UUID userId) {
        userService.permanentlyDeleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Send verification email to a user (admin only)
     */
    @PostMapping("/{userEmail}/send-verification")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> sendVerificationEmail(@PathVariable String userEmail) {
        userService.prepareUserForVerification(userService.getUserEntity(userEmail), true);
        authEmailService.sendVerificationEmail(userEmail);
        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }

    // ==================== DTOs ====================

    public record PasswordChangeRequest(String currentPassword, String newPassword) {
    }
}
