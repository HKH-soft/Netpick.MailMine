package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.model.PasswordHistory;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.PasswordHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordHistoryServiceTest {

    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordHistoryService passwordHistoryService;

    private UUID testUserId;
    private String testEmail;
    private String testPasswordHash;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "password@test.com";
        testPasswordHash = "hashed-password";

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail(testEmail);
    }

    @Nested
    @DisplayName("recordPassword Tests")
    class RecordPasswordTests {
        @Test
        @DisplayName("Should record password in history")
        void shouldRecordPassword() {
            when(passwordHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(passwordHistoryRepository.findRecentByUserId(testUserId)).thenReturn(List.of());

            passwordHistoryService.recordPassword(testUser, testPasswordHash);

            verify(passwordHistoryRepository).save(any(PasswordHistory.class));
        }
    }

    @Nested
    @DisplayName("isPasswordReused Tests")
    class IsPasswordReusedTests {
        @Test
        @DisplayName("Should return false when no history")
        void shouldReturnFalseWhenNoHistory() {
            when(passwordHistoryRepository.findRecentHashesByUserId(testUserId)).thenReturn(List.of());

            boolean result = passwordHistoryService.isPasswordReused(testUser, "newPassword");

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true when password matches history")
        void shouldReturnTrueWhenMatches() {
            when(passwordHistoryRepository.findRecentHashesByUserId(testUserId))
                    .thenReturn(List.of("oldHash1", "oldHash2"));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            boolean result = passwordHistoryService.isPasswordReused(testUser, "oldPassword");

            assertTrue(result);
            verify(passwordEncoder).matches("oldPassword", "oldHash1");
        }

        @Test
        @DisplayName("Should return false when password does not match")
        void shouldReturnFalseWhenNoMatch() {
            when(passwordHistoryRepository.findRecentHashesByUserId(testUserId))
                    .thenReturn(List.of("oldHash1", "oldHash2"));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            boolean result = passwordHistoryService.isPasswordReused(testUser, "newPassword");

            assertFalse(result);
        }
    }
}