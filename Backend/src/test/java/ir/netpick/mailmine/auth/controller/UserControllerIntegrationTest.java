package ir.netpick.mailmine.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.netpick.mailmine.auth.dto.AuthenticationResponse;
import ir.netpick.mailmine.auth.dto.AuthenticationSigninRequest;
import ir.netpick.mailmine.auth.dto.UserUpdateRequest;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User Controller Integration Tests")
class UserControllerIntegrationTest {

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
    private User regularUser;
    private User adminUser;
    private User superAdminUser;
    private String regularUserToken;
    private String adminToken;
    private String superAdminToken;

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

        // Get tokens for each user
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
    @DisplayName("Current User Operations")
    class CurrentUserOperationsTests {

        @Test
        @DisplayName("Should get current user profile successfully")
        void shouldGetCurrentUserProfile() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")
                    .header("Authorization", "Bearer " + regularUserToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("user@example.com"))
                    .andExpect(jsonPath("$.name").value("Regular User"))
                    .andExpect(jsonPath("$.isVerified").value(true));
        }

        @Test
        @DisplayName("Should fail to get profile without authentication")
        void shouldFailToGetProfileWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should update current user profile successfully")
        void shouldUpdateCurrentUserProfile() throws Exception {
            UserUpdateRequest updateRequest = new UserUpdateRequest("Updated Name", null, "Updated description");

            mockMvc.perform(put("/api/v1/users/me")
                    .header("Authorization", "Bearer " + regularUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Name"));

            // Verify the update
            User updatedUser = userRepository.findByEmail("user@example.com").orElseThrow();
            assertThat(updatedUser.getName()).isEqualTo("Updated Name");
            assertThat(updatedUser.getDescription()).isEqualTo("Updated description");
        }

        @Test
        @DisplayName("Should fail update when no changes provided")
        void shouldFailUpdateWhenNoChanges() throws Exception {
            UserUpdateRequest updateRequest = new UserUpdateRequest("Regular User", null, null);

            mockMvc.perform(put("/api/v1/users/me")
                    .header("Authorization", "Bearer " + regularUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() throws Exception {
            UserController.PasswordChangeRequest passwordRequest = new UserController.PasswordChangeRequest(
                    "password123", "newpassword123");

            mockMvc.perform(post("/api/v1/users/me/change-password")
                    .header("Authorization", "Bearer " + regularUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(passwordRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Password changed successfully"));

            // Verify new password works
            rateLimitingService.clearLoginAttempts("user@example.com");
            AuthenticationSigninRequest loginRequest = new AuthenticationSigninRequest("user@example.com",
                    "newpassword123");
            mockMvc.perform(post("/api/v1/auth/sign-in")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should fail change password with wrong current password")
        void shouldFailChangePasswordWithWrongCurrent() throws Exception {
            UserController.PasswordChangeRequest passwordRequest = new UserController.PasswordChangeRequest(
                    "wrongpassword", "newpassword123");

            mockMvc.perform(post("/api/v1/users/me/change-password")
                    .header("Authorization", "Bearer " + regularUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(passwordRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should delete current user account successfully")
        void shouldDeleteCurrentUserSuccessfully() throws Exception {
            mockMvc.perform(delete("/api/v1/users/me")
                    .header("Authorization", "Bearer " + regularUserToken))
                    .andExpect(status().isNoContent());

            // Verify user is soft deleted
            User deletedUser = userRepository.findByEmail("user@example.com").orElseThrow();
            assertThat(deletedUser.getDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Admin User Operations")
    class AdminUserOperationsTests {

        @Test
        @DisplayName("Admin should get all users successfully")
        void adminShouldGetAllUsers() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .param("page", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.context", notNullValue()))
                    .andExpect(jsonPath("$.context", hasSize(3))); // 3 users created in setup
        }

        @Test
        @DisplayName("Super Admin should get all users successfully")
        void superAdminShouldGetAllUsers() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                    .header("Authorization", "Bearer " + superAdminToken)
                    .param("page", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.context", notNullValue()));
        }

        @Test
        @DisplayName("Regular user should not get all users")
        void regularUserShouldNotGetAllUsers() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                    .header("Authorization", "Bearer " + regularUserToken)
                    .param("page", "1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Admin should get specific user by ID")
        void adminShouldGetSpecificUser() throws Exception {
            mockMvc.perform(get("/api/v1/users/{userId}", regularUser.getId())
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("user@example.com"))
                    .andExpect(jsonPath("$.name").value("Regular User"));
        }

        @Test
        @DisplayName("Admin should fail to get non-existent user")
        void adminShouldFailGetNonExistentUser() throws Exception {
            mockMvc.perform(get("/api/v1/users/{userId}", UUID.randomUUID())
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Admin should update user by ID")
        void adminShouldUpdateUserById() throws Exception {
            UserUpdateRequest updateRequest = new UserUpdateRequest("Admin Updated Name", null, null);

            mockMvc.perform(put("/api/v1/users/{userId}", regularUser.getId())
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Admin Updated Name"));
        }

        @Test
        @DisplayName("Admin should delete user by ID")
        void adminShouldDeleteUserById() throws Exception {
            mockMvc.perform(delete("/api/v1/users/{userId}", regularUser.getId())
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            // Verify user is soft deleted
            User deletedUser = userRepository.findById(regularUser.getId()).orElseThrow();
            assertThat(deletedUser.getDeleted()).isTrue();
        }

        @Test
        @DisplayName("Admin should restore deleted user")
        void adminShouldRestoreDeletedUser() throws Exception {
            // First delete the user
            regularUser.setDeleted(true);
            userRepository.save(regularUser);

            mockMvc.perform(post("/api/v1/users/{userId}/restore", regularUser.getId())
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User restored successfully"));

            // Verify user is restored
            User restoredUser = userRepository.findById(regularUser.getId()).orElseThrow();
            assertThat(restoredUser.getDeleted()).isFalse();
        }

        @Test
        @DisplayName("Regular user should not delete users")
        void regularUserShouldNotDeleteUsers() throws Exception {
            mockMvc.perform(delete("/api/v1/users/{userId}", adminUser.getId())
                    .header("Authorization", "Bearer " + regularUserToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Admin should send verification email to user")
        void adminShouldSendVerificationEmail() throws Exception {
            // Create an unverified user for this test
            User unverifiedUser = new User("unverified@example.com", passwordEncoder.encode("password123"),
                    "Unverified User", userRole);
            unverifiedUser.setIsVerified(false);
            userRepository.save(unverifiedUser);

            mockMvc.perform(post("/api/v1/users/{userEmail}/send-verification", "unverified@example.com")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Verification email sent"));
        }
    }

    @Nested
    @DisplayName("Super Admin Only Operations")
    class SuperAdminOnlyOperationsTests {

        @Test
        @DisplayName("Super Admin should permanently delete user")
        void superAdminShouldPermanentlyDeleteUser() throws Exception {
            UUID userIdToDelete = regularUser.getId();

            mockMvc.perform(delete("/api/v1/users/{userId}/permanent", userIdToDelete)
                    .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isNoContent());

            // Verify user is permanently deleted
            assertThat(userRepository.findById(userIdToDelete)).isEmpty();
        }

        @Test
        @DisplayName("Admin should not permanently delete user")
        void adminShouldNotPermanentlyDeleteUser() throws Exception {
            mockMvc.perform(delete("/api/v1/users/{userId}/permanent", regularUser.getId())
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Regular user should not permanently delete user")
        void regularUserShouldNotPermanentlyDeleteUser() throws Exception {
            mockMvc.perform(delete("/api/v1/users/{userId}/permanent", adminUser.getId())
                    .header("Authorization", "Bearer " + regularUserToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle invalid UUID format")
        void shouldHandleInvalidUuidFormat() throws Exception {
            mockMvc.perform(get("/api/v1/users/invalid-uuid")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return empty page when requesting high page number")
        void shouldReturnEmptyPageWhenNoUsersMatch() throws Exception {
            // Request a very high page number that has no users
            mockMvc.perform(get("/api/v1/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .param("page", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.context", hasSize(0)));
        }
    }
}
