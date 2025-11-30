package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.dto.UserDTO;
import ir.netpick.mailmine.auth.dto.UserUpdateRequest;
import ir.netpick.mailmine.auth.mapper.UserDTOMapper;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.repository.RoleRepository;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.common.enums.RoleEnum;
import ir.netpick.mailmine.common.exception.DuplicateResourceException;
import ir.netpick.mailmine.common.exception.RequestValidationException;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserDTOMapper userDTOMapper;

    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName(RoleEnum.USER);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPasswordHash("encoded-password");
        testUser.setRole(userRole);
        testUser.setIsVerified(true);
        testUser.setDeleted(false);

        // UserDTO(UUID id, String email, String name, RoleEnum role, Boolean
        // isVerified,
        // LocalDateTime created_at, LocalDateTime updatedAt, LocalDateTime lastLoginAt)
        testUserDTO = new UserDTO(
                testUser.getId(),
                testUser.getEmail(),
                "Test User",
                RoleEnum.USER,
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    @Nested
    @DisplayName("Get User Entity by Email Tests")
    class GetUserEntityByEmailTests {

        @Test
        @DisplayName("Should return user when found by email and not deleted")
        void shouldReturnUserWhenFoundAndNotDeleted() {
            when(userRepository.findByDeletedFalseAndEmail("test@example.com"))
                    .thenReturn(Optional.of(testUser));

            User result = userService.getUserEntity("test@example.com");

            assertThat(result).isEqualTo(testUser);
            verify(userRepository).findByDeletedFalseAndEmail("test@example.com");
            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByDeletedFalseAndEmail("nonexistent@example.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserEntity("nonexistent@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should not return deleted user")
        void shouldNotReturnDeletedUser() {
            // The findByDeletedFalseAndEmail query should filter out deleted users
            when(userRepository.findByDeletedFalseAndEmail("deleted@example.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserEntity("deleted@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get User Entity by UUID Tests")
    class GetUserEntityByUuidTests {

        @Test
        @DisplayName("Should return user when found by UUID")
        void shouldReturnUserWhenFoundByUuid() {
            UUID userId = testUser.getId();
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            User result = userService.getUserEntity(userId);

            assertThat(result).isEqualTo(testUser);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when UUID not found")
        void shouldThrowWhenUuidNotFound() {
            UUID randomId = UUID.randomUUID();
            when(userRepository.findById(randomId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserEntity(randomId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get User DTO by Email Tests")
    class GetUserDtoByEmailTests {

        @Test
        @DisplayName("Should return UserDTO when found")
        void shouldReturnUserDtoWhenFound() {
            when(userRepository.findByDeletedFalseAndEmail("test@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(userDTOMapper.apply(testUser)).thenReturn(testUserDTO);

            UserDTO result = userService.getUser("test@example.com");

            assertThat(result).isEqualTo(testUserDTO);
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("Should validate correct email format")
        void shouldValidateCorrectEmailFormat() {
            assertThat(userService.isEmailValidation("valid@example.com")).isTrue();
            assertThat(userService.isEmailValidation("user.name@domain.co.uk")).isTrue();
            assertThat(userService.isEmailValidation("user-name@domain.com")).isTrue();
            assertThat(userService.isEmailValidation("user_name@domain.com")).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid email format")
        void shouldRejectInvalidEmailFormat() {
            assertThat(userService.isEmailValidation("invalid")).isFalse();
            assertThat(userService.isEmailValidation("invalid@")).isFalse();
            assertThat(userService.isEmailValidation("@domain.com")).isFalse();
            assertThat(userService.isEmailValidation("no spaces@domain.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("Registration Request Validation Tests")
    class RegistrationValidationTests {

        @Test
        @DisplayName("Should throw DuplicateResourceException when email exists")
        void shouldThrowWhenEmailExists() {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "existing@example.com", "password123", "User");

            when(userRepository.existsUserByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.isRegisterRequestInvalid(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw RequestValidationException when email format is invalid")
        void shouldThrowWhenEmailFormatInvalid() {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "invalid-email", "password123", "User");

            when(userRepository.existsUserByEmail("invalid-email")).thenReturn(false);

            assertThatThrownBy(() -> userService.isRegisterRequestInvalid(request))
                    .isInstanceOf(RequestValidationException.class)
                    .hasMessageContaining("not valid");
        }

        @Test
        @DisplayName("Should throw RequestValidationException when password is null")
        void shouldThrowWhenPasswordIsNull() {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "valid@example.com", null, "User");

            assertThatThrownBy(() -> userService.isRegisterRequestInvalid(request))
                    .isInstanceOf(RequestValidationException.class)
                    .hasMessageContaining("required fields");
        }

        @Test
        @DisplayName("Should return false for valid request")
        void shouldReturnFalseForValidRequest() {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "new@example.com", "password123", "New User");

            when(userRepository.existsUserByEmail("new@example.com")).thenReturn(false);

            boolean result = userService.isRegisterRequestInvalid(request);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Create Unverified User Tests")
    class CreateUnverifiedUserTests {

        @Test
        @DisplayName("Should create unverified user successfully")
        void shouldCreateUnverifiedUserSuccessfully() {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "new@example.com", "password123", "New User");

            when(userRepository.existsUserByEmail("new@example.com")).thenReturn(false);
            when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = userService.createUnverifiedUser(request);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("new@example.com");
            assertThat(result.getPasswordHash()).isEqualTo("encoded-password");
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Soft Delete User Tests")
    class SoftDeleteUserTests {

        @Test
        @DisplayName("Should soft delete user by email")
        void shouldSoftDeleteUserByEmail() {
            when(userRepository.existsUserByEmail("test@example.com")).thenReturn(true);

            userService.deleteUser("test@example.com");

            verify(userRepository).updateDeletedByEmail(true, "test@example.com");
        }

        @Test
        @DisplayName("Should throw when user not found for deletion")
        void shouldThrowWhenUserNotFoundForDeletion() {
            when(userRepository.existsUserByEmail("nonexistent@example.com")).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser("nonexistent@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should soft delete user by UUID")
        void shouldSoftDeleteUserByUuid() {
            UUID userId = testUser.getId();
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.deleteUser(userId);

            assertThat(testUser.getDeleted()).isTrue();
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("Restore User Tests")
    class RestoreUserTests {

        @Test
        @DisplayName("Should restore soft-deleted user")
        void shouldRestoreSoftDeletedUser() {
            UUID userId = testUser.getId();
            testUser.setDeleted(true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.restoreUser(userId);

            assertThat(testUser.getDeleted()).isFalse();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw when user not found for restoration")
        void shouldThrowWhenUserNotFoundForRestoration() {
            UUID randomId = UUID.randomUUID();
            when(userRepository.findById(randomId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.restoreUser(randomId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            when(userRepository.findByDeletedFalseAndEmail("test@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("currentPassword", testUser.getPasswordHash()))
                    .thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("new-encoded-password");

            userService.changePassword("test@example.com", "currentPassword", "newPassword");

            assertThat(testUser.getPasswordHash()).isEqualTo("new-encoded-password");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw when current password is incorrect")
        void shouldThrowWhenCurrentPasswordIncorrect() {
            when(userRepository.findByDeletedFalseAndEmail("test@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", testUser.getPasswordHash()))
                    .thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword(
                    "test@example.com", "wrongPassword", "newPassword"))
                    .isInstanceOf(RequestValidationException.class)
                    .hasMessageContaining("incorrect");
        }

        @Test
        @DisplayName("Should throw when user not found for password change")
        void shouldThrowWhenUserNotFoundForPasswordChange() {
            when(userRepository.findByDeletedFalseAndEmail("nonexistent@example.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changePassword(
                    "nonexistent@example.com", "password", "newPassword"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user name")
        void shouldUpdateUserName() {
            // UserUpdateRequest(String name, String preference, String description)
            UserUpdateRequest request = new UserUpdateRequest("New Name", null, null);

            when(userRepository.findByDeletedFalseAndEmail("test@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userDTOMapper.apply(testUser)).thenReturn(testUserDTO);

            UserDTO result = userService.updateUser("test@example.com", request);

            assertThat(result).isNotNull();
            assertThat(testUser.getName()).isEqualTo("New Name");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw when no changes detected")
        void shouldThrowWhenNoChangesDetected() {
            testUser.setName("Same Name");
            UserUpdateRequest request = new UserUpdateRequest("Same Name", null, null);

            when(userRepository.findByDeletedFalseAndEmail("test@example.com"))
                    .thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> userService.updateUser("test@example.com", request))
                    .isInstanceOf(RequestValidationException.class)
                    .hasMessageContaining("No changes");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle email with plus sign")
        void shouldHandleEmailWithPlusSign() {
            assertThat(userService.isEmailValidation("user+tag@example.com")).isFalse();
        }

        @Test
        @DisplayName("Should handle email with subdomain")
        void shouldHandleEmailWithSubdomain() {
            assertThat(userService.isEmailValidation("user@mail.subdomain.example.com")).isTrue();
        }

        @Test
        @DisplayName("Should handle case sensitivity in email lookup")
        void shouldHandleCaseSensitivityInEmailLookup() {
            when(userRepository.findByDeletedFalseAndEmail("TEST@EXAMPLE.COM"))
                    .thenReturn(Optional.of(testUser));

            User result = userService.getUserEntity("TEST@EXAMPLE.COM");

            assertThat(result).isEqualTo(testUser);
        }

        @Test
        @DisplayName("Should prepare user for verification")
        void shouldPrepareUserForVerification() {
            when(verificationService.prepareUserForVerification(testUser)).thenReturn("ABCD1234");

            String code = userService.prepareUserForVerification(testUser);

            assertThat(code).isEqualTo("ABCD1234");
            verify(verificationService).prepareUserForVerification(testUser);
        }
    }
}
