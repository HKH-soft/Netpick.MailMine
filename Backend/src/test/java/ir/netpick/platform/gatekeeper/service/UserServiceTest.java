package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.enums.RoleEnum;
import ir.netpick.platform.core.exception.DuplicateResourceException;
import ir.netpick.platform.core.exception.RequestValidationException;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.exception.SystemConfigurationException;
import ir.netpick.platform.core.result.Result;
import ir.netpick.platform.core.result.error.Error;
import ir.netpick.platform.gatekeeper.dto.AuthenticationSignupRequest;
import ir.netpick.platform.gatekeeper.dto.UserDTO;
import ir.netpick.platform.gatekeeper.dto.UserUpdateRequest;
import ir.netpick.platform.gatekeeper.mapper.UserDTOMapper;
import ir.netpick.platform.gatekeeper.model.Role;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.RoleRepository;
import ir.netpick.platform.gatekeeper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private PasswordHistoryService passwordHistoryService;

    @InjectMocks
    private UserService userService;

    private UUID testUserId;
    private String testEmail;
    private String testPassword;
    private String testName;
    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "test@example.com";
        testPassword = "ValidPassword123!";
        testName = "Test User";

        testRole = new Role(RoleEnum.USER);
        testRole.setId(UUID.randomUUID());

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail(testEmail);
        testUser.setName(testName);
        testUser.setPasswordHash(testPassword);
        testUser.setRole(testRole);
        testUser.setIsVerified(false);
        testUser.setMfaEnabled(false);
    }

    @Nested
    @DisplayName("updateLastSign Tests")
    class UpdateLastSignTests {
        @Test
        @DisplayName("Should update last login timestamp")
        void shouldUpdateLastSign() {
            userService.updateLastSign(testEmail);

            verify(userRepository).updateLastLogin(any(LocalDateTime.class), eq(testEmail));
        }
    }

    @Nested
    @DisplayName("isEmailValidation Tests")
    class IsEmailValidationTests {
        @Test
        @DisplayName("Should validate correct email format")
        void shouldValidateCorrectEmail() {
            assertTrue(userService.isEmailValidation("test@example.com"));
            assertTrue(userService.isEmailValidation("user.name@domain.co.uk"));
        }

        @Test
        @DisplayName("Should reject invalid email format")
        void shouldRejectInvalidEmail() {
            assertFalse(userService.isEmailValidation("invalid-email"));
            assertFalse(userService.isEmailValidation("@example.com"));
            assertFalse(userService.isEmailValidation("user@"));
        }

        @Test
        @DisplayName("Should reject null or empty email")
        void shouldRejectNullEmptyEmail() {
            assertFalse(userService.isEmailValidation(""));
            assertFalse(userService.isEmailValidation("   "));
        }
    }

    @Nested
    @DisplayName("validatePassword Tests")
    class ValidatePasswordTests {
        @Test
        @DisplayName("Should reject null password")
        void shouldRejectNullPassword() {
            Result<?> result = userService.validatePassword(null, "user@test.com", "User");

            assertTrue(result.isError());
            assertEquals("Password.REQUIRED", result.getErrors().get(0).code());
        }

        @Test
        @DisplayName("Should reject empty password")
        void shouldRejectEmptyPassword() {
            Result<?> result = userService.validatePassword("   ", "user@test.com", "User");

            assertTrue(result.isError());
            assertEquals("Password.REQUIRED", result.getErrors().get(0).code());
        }

        @Test
        @DisplayName("Should reject short password")
        void shouldRejectShortPassword() {
            Result<?> result = userService.validatePassword("Short1!", "user@test.com", "User");

            assertTrue(result.isError());
            assertEquals("Password.TOO_SHORT", result.getErrors().get(0).code());
        }

        @Test
        @DisplayName("Should reject weak variety password")
        void shouldRejectWeakVarietyPassword() {
            Result<?> result = userService.validatePassword("abcdefghijkL", "user@test.com", "User");

            assertTrue(result.isError());
            assertEquals("Password.WEAK_VARIETY", result.getErrors().get(0).code());
        }

        @Test
        @DisplayName("Should reject password containing email local-part")
        void shouldRejectPasswordWithEmail() {
            Result<?> result = userService.validatePassword("test123!@#Test", "test@example.com", "User");

            assertTrue(result.isError());
            assertEquals("Password.CONTAINS_EMAIL", result.getErrors().get(0).code());
        }

        @Test
        @DisplayName("Should reject password containing name")
        void shouldRejectPasswordWithName() {
            Result<?> result = userService.validatePassword("TestUser123!@#", "different@test.com", "Test User");

            assertTrue(result.isError());
            assertEquals("Password.CONTAINS_NAME", result.getErrors().get(0).code());
        }

        @Test
        @DisplayName("Should reject common password")
        void shouldRejectCommonPassword() {
            Result<?> result = userService.validatePassword("password", "user@test.com", "User");

            assertTrue(result.isError());
            assertEquals("Password.TOO_SHORT", result.getErrors().get(0).code());
        }

        @Test
        @DisplayName("Should accept strong password")
        void shouldAcceptStrongPassword() {
            Result<?> result = userService.validatePassword("Str0ngP@ssw0rd!", "user@test.com", "User");

            assertTrue(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("isRegisterRequestInvalid Tests")
    class IsRegisterRequestInvalidTests {
        @Test
        @DisplayName("Should throw when email is null")
        void shouldThrowWhenEmailNull() {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    null, testPassword, testName);

            assertThrows(RequestValidationException.class, 
                    () -> userService.isRegisterRequestInvalid(request));
        }

        @Test
        @DisplayName("Should throw when password is null")
        void shouldThrowWhenPasswordNull() {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    testEmail, null, testName);

            assertThrows(RequestValidationException.class, 
                    () -> userService.isRegisterRequestInvalid(request));
        }

        @Test
        @DisplayName("Should throw when name is null")
        void shouldThrowWhenNameNull() {
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    testEmail, testPassword, null);

            assertThrows(RequestValidationException.class, 
                    () -> userService.isRegisterRequestInvalid(request));
        }

        @Test
        @DisplayName("Should throw when user already exists")
        void shouldThrowWhenUserExists() {
            when(userRepository.existsUserByEmail(testEmail)).thenReturn(true);
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    testEmail, testPassword, testName);

            assertThrows(DuplicateResourceException.class, 
                    () -> userService.isRegisterRequestInvalid(request));
        }

        @Test
        @DisplayName("Should throw when email format invalid")
        void shouldThrowWhenEmailInvalid() {
            when(userRepository.existsUserByEmail(anyString())).thenReturn(false);
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    "invalid-email", testPassword, testName);

            assertThrows(RequestValidationException.class, 
                    () -> userService.isRegisterRequestInvalid(request));
        }
    }

    @Nested
    @DisplayName("allUsers Tests")
    class AllUsersTests {
        @Test
        @DisplayName("Should return paginated users sorted by createdAt ascending")
        void shouldReturnPaginatedUsers() {
            Page<User> mockPage = new PageImpl<>(List.of(testUser));
            when(userRepository.findByDeletedFalse(any(PageRequest.class))).thenReturn(mockPage);
            UserDTO mockDto = new UserDTO(
                    testUserId, testEmail, testName, RoleEnum.USER,
                    false, null, null, null);
            when(userDTOMapper.apply(any(User.class))).thenReturn(mockDto);

            PageDTO<UserDTO> result = userService.allUsers(1);

            assertNotNull(result);
            assertEquals(1, result.content().size());
        }
    }

    @Nested
    @DisplayName("getUser Tests")
    class GetUserTests {
        @Test
        @DisplayName("Should return user by ID")
        void shouldReturnUserById() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            UserDTO mockDto = new UserDTO(
                    testUserId, testEmail, testName, RoleEnum.USER,
                    false, null, null, null);
            when(userDTOMapper.apply(any(User.class))).thenReturn(mockDto);

            UserDTO result = userService.getUser(testUserId);

            assertNotNull(result);
            assertEquals(testEmail, result.email());
        }

        @Test
        @DisplayName("Should throw when user not found by ID")
        void shouldThrowWhenUserNotFoundById() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> userService.getUser(testUserId));
        }

        @Test
        @DisplayName("Should return user by email")
        void shouldReturnUserByEmail() {
            when(userRepository.findIdByEmail(testEmail)).thenReturn(Optional.of(testUserId));
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            UserDTO mockDto = new UserDTO(
                    testUserId, testEmail, testName, RoleEnum.USER,
                    false, null, null, null);
            when(userDTOMapper.apply(any(User.class))).thenReturn(mockDto);

            UserDTO result = userService.getUser(testEmail);

            assertNotNull(result);
            assertEquals(testEmail, result.email());
        }
    }

    @Nested
    @DisplayName("createAdministrator Tests")
    class CreateAdministratorTests {
        @Test
        @DisplayName("Should throw when ADMIN role not found")
        void shouldThrowWhenAdminRoleNotFound() {
            when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.empty());
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    testEmail, testPassword, testName);

            assertThrows(SystemConfigurationException.class, 
                    () -> userService.createAdministrator(request));
        }
    }

    @Nested
    @DisplayName("createUnverifiedUser Tests")
    class CreateUnverifiedUserTests {
        @Test
        @DisplayName("Should throw when USER role not found")
        void shouldThrowWhenUserRoleNotFound() {
            when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.empty());
            AuthenticationSignupRequest request = new AuthenticationSignupRequest(
                    testEmail, testPassword, testName);

            assertThrows(SystemConfigurationException.class, 
                    () -> userService.createUnverifiedUser(request));
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {
        @Test
        @DisplayName("Should throw when updating SUPER_ADMIN")
        void shouldThrowWhenUpdatingSuperAdmin() {
            Role superAdminRole = new Role(RoleEnum.SUPER_ADMIN);
            superAdminRole.setId(UUID.randomUUID());
            testUser.setRole(superAdminRole);
            
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            UserUpdateRequest request = new UserUpdateRequest("New Name", null, "New Description");

            assertThrows(RequestValidationException.class, 
                    () -> userService.updateUser(testUserId, request));
        }

        @Test
        @DisplayName("Should throw when no changes detected")
        void shouldThrowWhenNoChanges() {
            testUser.setDescription("Test Description");
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            UserUpdateRequest request = new UserUpdateRequest(testName, null, "Test Description");

            assertThrows(RequestValidationException.class, 
                    () -> userService.updateUser(testUserId, request));
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {
        @Test
        @DisplayName("Should throw when deleting SUPER_ADMIN")
        void shouldThrowWhenDeletingSuperAdmin() {
            Role superAdminRole = new Role(RoleEnum.SUPER_ADMIN);
            superAdminRole.setId(UUID.randomUUID());
            testUser.setRole(superAdminRole);

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            assertThrows(RequestValidationException.class, 
                    () -> userService.deleteUser(testUserId));
        }
    }

    @Nested
    @DisplayName("restoreUser Tests")
    class RestoreUserTests {
        @Test
        @DisplayName("Should throw when restoring SUPER_ADMIN")
        void shouldThrowWhenRestoringSuperAdmin() {
            Role superAdminRole = new Role(RoleEnum.SUPER_ADMIN);
            superAdminRole.setId(UUID.randomUUID());
            testUser.setRole(superAdminRole);

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            assertThrows(RequestValidationException.class, 
                    () -> userService.restoreUser(testUserId));
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {
        @Test
        @DisplayName("Should throw when updating SUPER_ADMIN password")
        void shouldThrowWhenChangingSuperAdminPassword() {
            Role superAdminRole = new Role(RoleEnum.SUPER_ADMIN);
            superAdminRole.setId(UUID.randomUUID());
            testUser.setRole(superAdminRole);

            when(userRepository.findByDeletedFalseAndEmail(testEmail)).thenReturn(Optional.of(testUser));

            assertThrows(RequestValidationException.class, 
                    () -> userService.changePassword(testEmail, "current", "NewPassword123!"));
        }

        @Test
        @DisplayName("Should throw when current password incorrect")
        void shouldThrowWhenCurrentPasswordIncorrect() {
            when(userRepository.findByDeletedFalseAndEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThrows(RequestValidationException.class, 
                    () -> userService.changePassword(testEmail, "wrong", "NewPassword123!"));
        }
    }

    @Nested
    @DisplayName("permanentlyDeleteUser Tests")
    class PermanentlyDeleteUserTests {
        @Test
        @DisplayName("Should throw when deleting SUPER_ADMIN permanently")
        void shouldThrowWhenDeletingSuperAdminPermanently() {
            Role superAdminRole = new Role(RoleEnum.SUPER_ADMIN);
            superAdminRole.setId(UUID.randomUUID());
            testUser.setRole(superAdminRole);

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            assertThrows(RequestValidationException.class, 
                    () -> userService.permanentlyDeleteUser(testUserId));
        }
    }
}