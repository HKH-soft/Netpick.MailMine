package ir.netpick.mailmine.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.netpick.mailmine.auth.dto.AuthenticationResponse;
import ir.netpick.mailmine.auth.dto.AuthenticationSigninRequest;
import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.dto.RefreshTokenRequest;
import ir.netpick.mailmine.auth.dto.VerificationRequest;
import ir.netpick.mailmine.auth.email.AuthEmailService;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.model.Verification;
import ir.netpick.mailmine.auth.repository.RefreshTokenRepository;
import ir.netpick.mailmine.auth.repository.RoleRepository;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.auth.service.RateLimitingService;
import ir.netpick.mailmine.common.enums.RoleEnum;
import ir.netpick.mailmine.init.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Authentication Controller Integration Tests")
class AuthenticationControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private RefreshTokenRepository refreshTokenRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private RateLimitingService rateLimitingService;

        @MockitoBean
        private AuthEmailService authEmailService;

        private Role userRole;
        private Role adminRole;
        private Role superAdminRole;

        @BeforeEach
        void setUp() {
                // Clean up repositories
                refreshTokenRepository.deleteAll();
                userRepository.deleteAll();
                roleRepository.deleteAll();

                // Create roles
                userRole = new Role(RoleEnum.USER);
                userRole = roleRepository.save(userRole);

                adminRole = new Role(RoleEnum.ADMIN);
                adminRole = roleRepository.save(adminRole);

                superAdminRole = new Role(RoleEnum.SUPER_ADMIN);
                superAdminRole = roleRepository.save(superAdminRole);

                // Mock email service
                doNothing().when(authEmailService).sendVerificationEmail(anyString());
        }

        @Nested
        @DisplayName("Sign Up Tests")
        class SignUpTests {

                @Test
                @DisplayName("Should register new user successfully")
                void shouldRegisterNewUserSuccessfully() throws Exception {
                        AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                                        "newuser@example.com",
                                        "Password123!",
                                        "New User");

                        mockMvc.perform(post("/api/v1/auth/sign-up")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message")
                                                        .value(containsString("registered successfully")));

                        // Verify user was created
                        assertThat(userRepository.existsUserByEmail("newuser@example.com")).isTrue();
                }

                @Test
                @DisplayName("Should fail when email already exists")
                void shouldFailWhenEmailAlreadyExists() throws Exception {
                        // Create existing user
                        User existingUser = new User("existing@example.com", passwordEncoder.encode("password"),
                                        "Existing User",
                                        userRole);
                        existingUser.setIsVerified(true);
                        userRepository.save(existingUser);

                        AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                                        "existing@example.com",
                                        "Password123!",
                                        "New User");

                        mockMvc.perform(post("/api/v1/auth/sign-up")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isConflict());
                }

                @Test
                @DisplayName("Should fail when email format is invalid")
                void shouldFailWhenEmailFormatIsInvalid() throws Exception {
                        AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                                        "invalid-email",
                                        "Password123!",
                                        "New User");

                        mockMvc.perform(post("/api/v1/auth/sign-up")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should fail when password is missing")
                void shouldFailWhenPasswordIsMissing() throws Exception {
                        AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                                        "newuser@example.com",
                                        null,
                                        "New User");

                        mockMvc.perform(post("/api/v1/auth/sign-up")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("Sign In Tests")
        class SignInTests {

                private User verifiedUser;

                @BeforeEach
                void setUpUser() {
                        verifiedUser = new User("verified@example.com", passwordEncoder.encode("password123"),
                                        "Verified User",
                                        userRole);
                        verifiedUser.setIsVerified(true);
                        verifiedUser = userRepository.save(verifiedUser);

                        // Clear any rate limiting
                        rateLimitingService.clearLoginAttempts("verified@example.com");
                }

                @Test
                @DisplayName("Should sign in successfully with valid credentials")
                void shouldSignInSuccessfully() throws Exception {
                        AuthenticationSigninRequest request = new AuthenticationSigninRequest(
                                        "verified@example.com",
                                        "password123");

                        mockMvc.perform(post("/api/v1/auth/sign-in")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.access_token", notNullValue()))
                                        .andExpect(jsonPath("$.refresh_token", notNullValue()))
                                        .andExpect(jsonPath("$.expires_in", notNullValue()))
                                        .andExpect(jsonPath("$.token_type").value("Bearer"));
                }

                @Test
                @DisplayName("Should fail with incorrect password")
                void shouldFailWithIncorrectPassword() throws Exception {
                        AuthenticationSigninRequest request = new AuthenticationSigninRequest(
                                        "verified@example.com",
                                        "wrongpassword");

                        mockMvc.perform(post("/api/v1/auth/sign-in")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("Should fail with non-existent user")
                void shouldFailWithNonExistentUser() throws Exception {
                        AuthenticationSigninRequest request = new AuthenticationSigninRequest(
                                        "nonexistent@example.com",
                                        "password123");

                        mockMvc.perform(post("/api/v1/auth/sign-in")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("Should fail when user is not verified")
                void shouldFailWhenUserNotVerified() throws Exception {
                        User unverifiedUser = new User("unverified@example.com", passwordEncoder.encode("password123"),
                                        "Unverified User", userRole);
                        unverifiedUser.setIsVerified(false);
                        userRepository.save(unverifiedUser);
                        rateLimitingService.clearLoginAttempts("unverified@example.com");

                        AuthenticationSigninRequest request = new AuthenticationSigninRequest(
                                        "unverified@example.com",
                                        "password123");

                        mockMvc.perform(post("/api/v1/auth/sign-in")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("Should allow unverified SUPER_ADMIN to sign in")
                void shouldAllowUnverifiedSuperAdminToSignIn() throws Exception {
                        User superAdmin = new User("superadmin@example.com", passwordEncoder.encode("password123"),
                                        "Super Admin",
                                        superAdminRole);
                        superAdmin.setIsVerified(false);
                        userRepository.save(superAdmin);
                        rateLimitingService.clearLoginAttempts("superadmin@example.com");

                        AuthenticationSigninRequest request = new AuthenticationSigninRequest(
                                        "superadmin@example.com",
                                        "password123");

                        mockMvc.perform(post("/api/v1/auth/sign-in")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.access_token", notNullValue()));
                }
        }

        @Nested
        @DisplayName("Verification Tests")
        class VerificationTests {

                @Test
                @DisplayName("Should verify user successfully with valid code")
                void shouldVerifyUserSuccessfully() throws Exception {
                        User user = new User("toverify@example.com", passwordEncoder.encode("password123"),
                                        "To Verify User",
                                        userRole);
                        user.setIsVerified(false);
                        Verification verification = new Verification("ABCD1234");
                        user.setVerification(verification);
                        user = userRepository.save(user);
                        rateLimitingService.clearVerificationAttempts("toverify@example.com");

                        VerificationRequest request = new VerificationRequest("toverify@example.com", "ABCD1234");

                        mockMvc.perform(post("/api/v1/auth/verify")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message")
                                                        .value(containsString("verified successfully")));

                        // Verify user is now verified
                        User updatedUser = userRepository.findByEmail("toverify@example.com").orElseThrow();
                        assertThat(updatedUser.getIsVerified()).isTrue();
                        assertThat(updatedUser.getVerification()).isNull();
                }

                @Test
                @DisplayName("Should fail verification with invalid code")
                void shouldFailVerificationWithInvalidCode() throws Exception {
                        User user = new User("toverify2@example.com", passwordEncoder.encode("password123"),
                                        "To Verify User",
                                        userRole);
                        user.setIsVerified(false);
                        Verification verification = new Verification("ABCD1234");
                        user.setVerification(verification);
                        userRepository.save(user);
                        rateLimitingService.clearVerificationAttempts("toverify2@example.com");

                        VerificationRequest request = new VerificationRequest("toverify2@example.com", "WRONG123");

                        mockMvc.perform(post("/api/v1/auth/verify")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should fail verification for non-existent user")
                void shouldFailVerificationForNonExistentUser() throws Exception {
                        rateLimitingService.clearVerificationAttempts("nonexistent@example.com");

                        VerificationRequest request = new VerificationRequest("nonexistent@example.com", "ABCD1234");

                        mockMvc.perform(post("/api/v1/auth/verify")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("Resend Verification Tests")
        class ResendVerificationTests {

                @Test
                @DisplayName("Should resend verification email successfully")
                void shouldResendVerificationSuccessfully() throws Exception {
                        User user = new User("resend@example.com", passwordEncoder.encode("password123"), "Resend User",
                                        userRole);
                        user.setIsVerified(false);
                        Verification verification = new Verification("OLDCODE1");
                        user.setVerification(verification);
                        userRepository.save(user);
                        rateLimitingService.clearResendAttempts("resend@example.com");

                        mockMvc.perform(post("/api/v1/auth/resend-verification")
                                        .param("email", "resend@example.com"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value(containsString("sent successfully")));
                }

                @Test
                @DisplayName("Should fail resend for already verified user")
                void shouldFailResendForVerifiedUser() throws Exception {
                        User user = new User("alreadyverified@example.com", passwordEncoder.encode("password123"),
                                        "Already Verified User", userRole);
                        user.setIsVerified(true);
                        userRepository.save(user);
                        rateLimitingService.clearResendAttempts("alreadyverified@example.com");

                        mockMvc.perform(post("/api/v1/auth/resend-verification")
                                        .param("email", "alreadyverified@example.com"))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should fail resend for non-existent user")
                void shouldFailResendForNonExistentUser() throws Exception {
                        rateLimitingService.clearResendAttempts("nonexistent@example.com");

                        mockMvc.perform(post("/api/v1/auth/resend-verification")
                                        .param("email", "nonexistent@example.com"))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("Token Refresh Tests")
        class TokenRefreshTests {

                @Test
                @DisplayName("Should refresh token successfully")
                void shouldRefreshTokenSuccessfully() throws Exception {
                        // First sign in to get tokens
                        User user = new User("refreshtest@example.com", passwordEncoder.encode("password123"),
                                        "Refresh Test User",
                                        userRole);
                        user.setIsVerified(true);
                        userRepository.save(user);
                        rateLimitingService.clearLoginAttempts("refreshtest@example.com");

                        AuthenticationSigninRequest signInRequest = new AuthenticationSigninRequest(
                                        "refreshtest@example.com",
                                        "password123");

                        MvcResult signInResult = mockMvc.perform(post("/api/v1/auth/sign-in")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(signInRequest)))
                                        .andExpect(status().isOk())
                                        .andReturn();

                        AuthenticationResponse signInResponse = objectMapper.readValue(
                                        signInResult.getResponse().getContentAsString(),
                                        AuthenticationResponse.class);

                        // Use refresh token to get new access token
                        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(signInResponse.refreshToken());

                        mockMvc.perform(post("/api/v1/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(refreshRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.access_token", notNullValue()))
                                        .andExpect(jsonPath("$.refresh_token", notNullValue()));
                }

                @Test
                @DisplayName("Should fail with invalid refresh token")
                void shouldFailWithInvalidRefreshToken() throws Exception {
                        RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");

                        mockMvc.perform(post("/api/v1/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isUnauthorized());
                }
        }

        @Nested
        @DisplayName("Logout Tests")
        class LogoutTests {

                @Test
                @DisplayName("Should logout successfully")
                void shouldLogoutSuccessfully() throws Exception {
                        // First sign in
                        User user = new User("logouttest@example.com", passwordEncoder.encode("password123"),
                                        "Logout Test User",
                                        userRole);
                        user.setIsVerified(true);
                        userRepository.save(user);
                        rateLimitingService.clearLoginAttempts("logouttest@example.com");

                        AuthenticationSigninRequest signInRequest = new AuthenticationSigninRequest(
                                        "logouttest@example.com",
                                        "password123");

                        MvcResult signInResult = mockMvc.perform(post("/api/v1/auth/sign-in")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(signInRequest)))
                                        .andExpect(status().isOk())
                                        .andReturn();

                        AuthenticationResponse signInResponse = objectMapper.readValue(
                                        signInResult.getResponse().getContentAsString(),
                                        AuthenticationResponse.class);

                        // Logout
                        RefreshTokenRequest logoutRequest = new RefreshTokenRequest(signInResponse.refreshToken());

                        mockMvc.perform(post("/api/v1/auth/logout")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(logoutRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message")
                                                        .value(containsString("Logged out successfully")));

                        // Verify refresh token is revoked by checking the database directly
                        // The token should be marked as revoked
                        assertThat(refreshTokenRepository.findByToken(signInResponse.refreshToken()))
                                        .isPresent()
                                        .hasValueSatisfying(token -> assertThat(token.isRevoked()).isTrue());
                }
        }

        @Nested
        @DisplayName("Logout All Devices Tests")
        class LogoutAllDevicesTests {

                @Test
                @DisplayName("Should logout from all devices successfully")
                void shouldLogoutFromAllDevicesSuccessfully() throws Exception {
                        // First sign in
                        User user = new User("logoutalltest@example.com", passwordEncoder.encode("password123"),
                                        "Logout All Test User", userRole);
                        user.setIsVerified(true);
                        userRepository.save(user);
                        rateLimitingService.clearLoginAttempts("logoutalltest@example.com");

                        AuthenticationSigninRequest signInRequest = new AuthenticationSigninRequest(
                                        "logoutalltest@example.com",
                                        "password123");

                        MvcResult signInResult = mockMvc.perform(post("/api/v1/auth/sign-in")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(signInRequest)))
                                        .andExpect(status().isOk())
                                        .andReturn();

                        AuthenticationResponse signInResponse = objectMapper.readValue(
                                        signInResult.getResponse().getContentAsString(),
                                        AuthenticationResponse.class);

                        // Logout from all devices
                        mockMvc.perform(post("/api/v1/auth/logout-all")
                                        .header("Authorization", "Bearer " + signInResponse.accessToken()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value(containsString("all devices")));
                }

                @Test
                @DisplayName("Should fail logout all without authentication")
                void shouldFailLogoutAllWithoutAuthentication() throws Exception {
                        mockMvc.perform(post("/api/v1/auth/logout-all"))
                                        .andExpect(status().isUnauthorized());
                }
        }
}
