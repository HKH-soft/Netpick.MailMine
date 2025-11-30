package ir.netpick.mailmine.auth.controller;

import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.service.AdminService;
import ir.netpick.mailmine.common.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * Create a new user account (admin-initiated, skips normal signup)
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AuthenticationSignupRequest request) {
        adminService.createUser(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Create a new admin account (super admin only)
     */
    @PostMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createAdmin(@RequestBody AuthenticationSignupRequest request) {
        adminService.createUserWithRole(request, RoleEnum.ADMIN);
        return ResponseEntity.ok().build();
    }
}