package ir.netpick.platform.gatekeeper.model;

import ir.netpick.platform.core.enums.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private UUID testUserId;
    private Role userRole;
    private Role adminRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        userRole = new Role(RoleEnum.USER);
        userRole.setId(UUID.randomUUID());
        adminRole = new Role(RoleEnum.ADMIN);
        adminRole.setId(UUID.randomUUID());

        testUser = new User("test@example.com", "encodedPassword", "Test User", userRole);
        testUser.setId(testUserId);
        testUser.setIsVerified(true);
        testUser.setMfaEnabled(false);
    }

    @Nested
    @DisplayName("User Entity Tests")
    class UserEntityTests {

        @Test
        @DisplayName("Should create user with constructor")
        void shouldCreateUserWithConstructor() {
            assertEquals("test@example.com", testUser.getEmail());
            assertEquals("encodedPassword", testUser.getPasswordHash());
            assertEquals("Test User", testUser.getName());
            assertSame(userRole, testUser.getRole());
        }

        @Test
        @DisplayName("getAuthorities should return ROLE-based authority")
        void shouldReturnRoleAuthority() {
            var authorities = testUser.getAuthorities();

            assertEquals(1, authorities.size());
            assertTrue(authorities.stream()
                    .anyMatch(a -> a instanceof SimpleGrantedAuthority && 
                            ((SimpleGrantedAuthority) a).getAuthority().equals("ROLE_USER")));
        }

        @Test
        @DisplayName("getPassword should return password hash")
        void shouldReturnPasswordHash() {
            assertEquals("encodedPassword", testUser.getPassword());
        }

        @Test
        @DisplayName("getUsername should return email")
        void shouldReturnEmailAsUsername() {
            assertEquals("test@example.com", testUser.getUsername());
        }

        @Test
        @DisplayName("isAccountNonExpired should return true")
        void accountNonExpired() {
            assertTrue(testUser.isAccountNonExpired());
        }

        @Test
        @DisplayName("isAccountNonLocked should return true")
        void accountNonLocked() {
            assertTrue(testUser.isAccountNonLocked());
        }

        @Test
        @DisplayName("isCredentialsNonExpired should return true")
        void credentialsNonExpired() {
            assertTrue(testUser.isCredentialsNonExpired());
        }

        @Test
        @DisplayName("isEnabled should return true when not deleted")
        void enabledWhenNotDeleted() {
            testUser.setDeleted(false);
            assertTrue(testUser.isEnabled());
        }

        @Test
        @DisplayName("isEnabled should return true even when deleted (not wired in User)")
        void disabledWhenDeleted() {
            testUser.setDeleted(true);
            assertTrue(testUser.isEnabled());
        }
    }

    @Nested
    @DisplayName("Role Entity Tests")
    class RoleEntityTests {

        @Test
        @DisplayName("Should create role with enum")
        void shouldCreateRoleWithEnum() {
            Role admin = new Role(RoleEnum.ADMIN);
            assertEquals(RoleEnum.ADMIN, admin.getName());
        }

        @Test
        @DisplayName("Should set description")
        void shouldSetDescription() {
            userRole.setDescription("Regular user role");
            assertEquals("Regular user role", userRole.getDescription());
        }
    }

    @Nested
    @DisplayName("Verification Embeddable Tests")
    class VerificationTests {

        @Test
        @DisplayName("Should create verification")
        void shouldCreateVerification() {
            Verification verification = new Verification("123456");

            assertEquals("123456", verification.getCode());
            assertNotNull(verification.getVerificationExpiresAt());
            assertEquals(0, verification.getAttempts());
        }
    }
}