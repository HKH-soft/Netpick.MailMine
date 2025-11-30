package ir.netpick.mailmine.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.netpick.mailmine.auth.dto.AuthenticationResponse;
import ir.netpick.mailmine.auth.dto.AuthenticationSigninRequest;
import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.email.AuthEmailService;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Admin Controller Integration Tests")
class AdminControllerIntegrationTest {

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
    private User adminUser;
    private User superAdminUser;
    private User regularUser;
    private String adminToken;
    private String superAdminToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() throws Exception {
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

        // Create test users
        regularUser = new User("user@example.com", passwordEncoder.encode("password123"), "Regular User", userRole);
        regularUser.setIsVerified(true);
        regularUser = userRepository.save(regularUser);

        adminUser = new User("admin@example.com", passwordEncoder.encode("password123"), "Admin User", adminRole);
        adminUser.setIsVerified(true);
        adminUser = userRepository.save(adminUser);

        superAdminUser = new User("superadmin@example.com", passwordEncoder.encode("password123"), "Super Admin User",
                superAdminRole);
        superAdminUser.setIsVerified(true);
        superAdminUser = userRepository.save(superAdminUser);

        // Clear rate limiting
        rateLimitingService.clearLoginAttempts("user@example.com");
        rateLimitingService.clearLoginAttempts("admin@example.com");
        rateLimitingService.clearLoginAttempts("superadmin@example.com");

        // Get tokens
        regularUserToken = getToken("user@example.com", "password123");
        adminToken = getToken("admin@example.com", "password123");
        superAdminToken = getToken("superadmin@example.com", "password123");
    }

    private String getToken(String email, String password) throws Exception {
        AuthenticationSigninRequest request = new AuthenticationSigninRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        AuthenticationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class);
        return response.accessToken();
    }

    @Nested
    @DisplayName("Create User Tests (Admin)")
    class CreateUserTests {

        @Test
        @DisplayName("Admin should create new user successfully")
        void adminShouldCreateNewUser() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "newuser@example.com",
                    "Password123!",
                    "New User Created by Admin");

            mockMvc.perform(post("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Verify user was created
            assertThat(userRepository.existsUserByEmail("newuser@example.com")).isTrue();

            // Verify user is not verified (admin-created users need verification)
            User createdUser = userRepository.findByEmail("newuser@example.com").orElseThrow();
            assertThat(createdUser.getIsVerified()).isFalse();
        }

        @Test
        @DisplayName("Super Admin should create new user successfully")
        void superAdminShouldCreateNewUser() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "anotheruser@example.com",
                    "Password123!",
                    "Another New User");

            mockMvc.perform(post("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + superAdminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            assertThat(userRepository.existsUserByEmail("anotheruser@example.com")).isTrue();
        }

        @Test
        @DisplayName("Regular user should not create users")
        void regularUserShouldNotCreateUsers() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "unauthorized@example.com",
                    "Password123!",
                    "Unauthorized User");

            mockMvc.perform(post("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + regularUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            // Verify user was NOT created
            assertThat(userRepository.existsUserByEmail("unauthorized@example.com")).isFalse();
        }

        @Test
        @DisplayName("Should fail when creating user with existing email")
        void shouldFailWhenEmailExists() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "user@example.com", // Already exists
                    "Password123!",
                    "Duplicate User");

            mockMvc.perform(post("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should fail when creating user with invalid email")
        void shouldFailWithInvalidEmail() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "invalid-email",
                    "Password123!",
                    "Invalid Email User");

            mockMvc.perform(post("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail without authentication")
        void shouldFailWithoutAuthentication() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "noauth@example.com",
                    "Password123!",
                    "No Auth User");

            mockMvc.perform(post("/api/v1/admin/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Create Admin Tests (Super Admin Only)")
    class CreateAdminTests {

        @Test
        @DisplayName("Super Admin should create new admin successfully")
        void superAdminShouldCreateAdmin() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "newadmin@example.com",
                    "AdminPass123!",
                    "New Admin User");

            mockMvc.perform(post("/api/v1/admin/admins")
                    .header("Authorization", "Bearer " + superAdminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Verify admin was created
            assertThat(userRepository.existsUserByEmail("newadmin@example.com")).isTrue();

            // Verify user has admin role and is verified
            User createdAdmin = userRepository.findByEmail("newadmin@example.com").orElseThrow();
            assertThat(createdAdmin.getRole().getName()).isEqualTo(RoleEnum.ADMIN);
            assertThat(createdAdmin.getIsVerified()).isTrue(); // Admins are auto-verified
        }

        @Test
        @DisplayName("Regular Admin should NOT create other admins")
        void regularAdminShouldNotCreateAdmins() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "unauthorizedadmin@example.com",
                    "AdminPass123!",
                    "Unauthorized Admin");

            mockMvc.perform(post("/api/v1/admin/admins")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            // Verify admin was NOT created
            assertThat(userRepository.existsUserByEmail("unauthorizedadmin@example.com")).isFalse();
        }

        @Test
        @DisplayName("Regular user should NOT create admins")
        void regularUserShouldNotCreateAdmins() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "hacker@example.com",
                    "HackerPass123!",
                    "Hacker Admin");

            mockMvc.perform(post("/api/v1/admin/admins")
                    .header("Authorization", "Bearer " + regularUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            assertThat(userRepository.existsUserByEmail("hacker@example.com")).isFalse();
        }

        @Test
        @DisplayName("Should fail when creating admin with existing email")
        void shouldFailWhenAdminEmailExists() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "admin@example.com", // Already exists
                    "AdminPass123!",
                    "Duplicate Admin");

            mockMvc.perform(post("/api/v1/admin/admins")
                    .header("Authorization", "Bearer " + superAdminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should fail without authentication")
        void shouldFailWithoutAuthentication() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "noauthadmin@example.com",
                    "AdminPass123!",
                    "No Auth Admin");

            mockMvc.perform(post("/api/v1/admin/admins")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws Exception {
            mockMvc.perform(post("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle null values in request")
        void shouldHandleNullValues() throws Exception {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "nulltest@example.com",
                    null, // null password
                    "Null Password User");

            mockMvc.perform(post("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Created user should be able to sign in after verification")
        void createdUserShouldSignInAfterVerification() throws Exception {
            // Admin creates user
            AuthenticationSignupRequest createRequest = new AuthenticationSignupRequest(
                    "logintest@example.com",
                    "TestPass123!",
                    "Login Test User");

            mockMvc.perform(post("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk());

            // Manually verify the user (simulating email verification)
            User createdUser = userRepository.findByEmail("logintest@example.com").orElseThrow();
            createdUser.setIsVerified(true);
            createdUser.setVerification(null);
            userRepository.save(createdUser);

            // User should now be able to sign in
            rateLimitingService.clearLoginAttempts("logintest@example.com");
            AuthenticationSigninRequest signInRequest = new AuthenticationSigninRequest(
                    "logintest@example.com",
                    "TestPass123!");

            mockMvc.perform(post("/api/v1/auth/sign-in")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signInRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Created admin should be able to sign in immediately (auto-verified)")
        void createdAdminShouldSignInImmediately() throws Exception {
            // Super Admin creates admin
            AuthenticationSignupRequest createRequest = new AuthenticationSignupRequest(
                    "newadminlogin@example.com",
                    "AdminPass123!",
                    "New Admin Login Test");

            mockMvc.perform(post("/api/v1/admin/admins")
                    .header("Authorization", "Bearer " + superAdminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk());

            // Admin should be able to sign in immediately
            rateLimitingService.clearLoginAttempts("newadminlogin@example.com");
            AuthenticationSigninRequest signInRequest = new AuthenticationSigninRequest(
                    "newadminlogin@example.com",
                    "AdminPass123!");

            mockMvc.perform(post("/api/v1/auth/sign-in")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signInRequest)))
                    .andExpect(status().isOk());
        }
    }
}
