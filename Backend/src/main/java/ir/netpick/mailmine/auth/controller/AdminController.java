package ir.netpick.mailmine.auth.controller;

import ir.netpick.mailmine.auth.dto.AllUsersResponse;
import ir.netpick.mailmine.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.service.AuthenticationService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("createAdmin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createAdministrator(@RequestBody AuthenticationSignupRequest request) {

        String jwtToken = authenticationService.registerAdmin(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .build();

    }
    
    @GetMapping("users")
    public ResponseEntity<AllUsersResponse> getUsers(@RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok()
                .body(userService.allUsers(page));
    }


}
