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

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AuthenticationSignupRequest request) {
        adminService.createAdminUser(request);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createAdminUser(@RequestBody AuthenticationSignupRequest request) {
        adminService.createAdminUser(request, RoleEnum.ADMIN);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/users/{userEmail}/send-verification")
    public ResponseEntity<?> sendVerificationEmail(@PathVariable String userEmail) {
        adminService.sendVerificationEmailToUser(userEmail);
        return ResponseEntity.ok().build();
    }
}