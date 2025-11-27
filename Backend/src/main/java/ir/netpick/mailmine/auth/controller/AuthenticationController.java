package ir.netpick.mailmine.auth.controller;

import ir.netpick.mailmine.auth.dto.AuthenticationResponse;
import ir.netpick.mailmine.auth.dto.AuthenticationSigninRequest;
import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.dto.MessageResponse;
import ir.netpick.mailmine.auth.dto.VerificationRequest;
import ir.netpick.mailmine.auth.email.EmailDetails;
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
    public ResponseEntity<MessageResponse> verify(@RequestBody VerificationRequest request) {
        authenticationService.verifyUser(request);
        return ResponseEntity.ok(new MessageResponse("User verified successfully"));
    }
    
    @PostMapping("resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@RequestParam String email) {
        authenticationService.resendVerification(email);
        return ResponseEntity.ok(new MessageResponse("Verification email sent successfully"));
    }
    
    @PostMapping("sign-up")
    public ResponseEntity<MessageResponse> signUp(@RequestBody AuthenticationSignupRequest request) {
        authenticationService.signUp(request);
        return ResponseEntity.ok(new MessageResponse("User registered successfully. Please check your email for verification."));
    }
    
    @PostMapping("sign-in")
    public ResponseEntity<MessageResponse> signIn(@RequestBody AuthenticationSigninRequest request) {
        String jwtToken = authenticationService.signIn(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .body(new MessageResponse("Logged in successfully"));
    }

}