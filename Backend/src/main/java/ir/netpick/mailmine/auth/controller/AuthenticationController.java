package ir.netpick.mailmine.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import ir.netpick.mailmine.auth.dto.AuthenticationResponse;
import ir.netpick.mailmine.auth.dto.AuthenticationSigninRequest;
import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.dto.MessageResponse;
import ir.netpick.mailmine.auth.dto.RefreshTokenRequest;
import ir.netpick.mailmine.auth.dto.VerificationRequest;
import ir.netpick.mailmine.auth.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Verify email", description = "Verify user's email address using the verification code sent to their email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired verification code"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "429", description = "Too many verification attempts")
    })
    @PostMapping("verify")
    public ResponseEntity<MessageResponse> verify(@RequestBody VerificationRequest request) {
        authenticationService.verifyUser(request);
        return ResponseEntity.ok(new MessageResponse("User verified successfully"));
    }

    @Operation(summary = "Resend verification email", description = "Resend a new verification code to the user's email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification email sent successfully"),
            @ApiResponse(responseCode = "400", description = "User already verified"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "429", description = "Too many resend attempts")
    })
    @PostMapping("resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@RequestParam String email) {
        authenticationService.resendVerification(email);
        return ResponseEntity.ok(new MessageResponse("Verification email sent successfully"));
    }

    @Operation(summary = "Register new user", description = "Create a new user account. A verification email will be sent to the provided email address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or email already exists"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping("sign-up")
    public ResponseEntity<MessageResponse> signUp(@RequestBody AuthenticationSignupRequest request) {
        authenticationService.signUp(request);
        return ResponseEntity
                .ok(new MessageResponse("User registered successfully. Please check your email for verification."));
    }

    @Operation(summary = "Sign in", description = "Authenticate user and receive access and refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or account not verified"),
            @ApiResponse(responseCode = "429", description = "Too many failed login attempts")
    })
    @PostMapping("sign-in")
    public ResponseEntity<AuthenticationResponse> signIn(
            @RequestBody AuthenticationSigninRequest request,
            HttpServletRequest httpRequest) {
        String deviceInfo = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);

        AuthenticationResponse response = authenticationService.signIn(request, deviceInfo, ipAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token", description = "Get a new access token using a valid refresh token. The old refresh token will be rotated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        String deviceInfo = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);

        AuthenticationResponse response = authenticationService.refreshAccessToken(
                request.refreshToken(), deviceInfo, ipAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout", description = "Invalidate the refresh token to logout from the current device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully")
    })
    @PostMapping("logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authenticationService.logout(request.refreshToken());
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @Operation(summary = "Logout from all devices", description = "Invalidate all refresh tokens for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out from all devices successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("logout-all")
    public ResponseEntity<MessageResponse> logoutAllDevices(@AuthenticationPrincipal UserDetails userDetails) {
        authenticationService.logoutAllDevices(userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Logged out from all devices successfully"));
    }

    /**
     * Extract client IP address from request, considering proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For may contain multiple IPs, get the first one
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

}