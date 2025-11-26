package ir.netpick.mailmine.auth.controller;

import ir.netpick.mailmine.auth.dto.AuthenticationSigninRequest;
import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.dto.VerificationRequest;
import ir.netpick.mailmine.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    
    @PostMapping("verify")
    public ResponseEntity<?> verify(@RequestBody VerificationRequest request) {
        authenticationService.verifyUser(request);
        return ResponseEntity.ok()
                .build();
    }
    
    @PostMapping("resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        authenticationService.resendVerification(email);
        return ResponseEntity.ok()
                .build();
    }
    
    @PostMapping("sign-up")
    public ResponseEntity<?> signUp(@RequestBody AuthenticationSignupRequest request) {
        authenticationService.signUp(request);
        return ResponseEntity.ok()
                .build();
    }
    
    @PostMapping("sign-in")
    public ResponseEntity<?> signIn(@RequestBody AuthenticationSigninRequest request) {
        String jwtToken = authenticationService.signIn(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .build();
    }

}