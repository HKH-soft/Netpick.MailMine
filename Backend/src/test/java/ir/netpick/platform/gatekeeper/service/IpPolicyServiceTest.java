package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.core.exception.RequestValidationException;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.gatekeeper.dto.IpPolicyCreateRequest;
import ir.netpick.platform.gatekeeper.model.IpPolicy;
import ir.netpick.platform.gatekeeper.model.SecurityEvent;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.IpPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpPolicyServiceTest {

    @Mock
    private IpPolicyRepository ipPolicyRepository;

    @Mock
    private SecurityEventService securityEventService;

    @InjectMocks
    private IpPolicyService ipPolicyService;

    private UUID testPolicyId;
    private UUID testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        testPolicyId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("policy@test.com");
    }

    @Nested
    @DisplayName("createPolicy Tests")
    class CreatePolicyTests {
        @Test
        @DisplayName("Should throw for invalid policy type")
        void shouldThrowForInvalidType() {
            IpPolicyCreateRequest request = new IpPolicyCreateRequest(
                    "Test Policy", "INVALID", null, null, null, null, null, null);

            assertThrows(RequestValidationException.class, 
                    () -> ipPolicyService.createPolicy(request, testUser));
        }

        @Test
        @DisplayName("Should throw when no IP specification")
        void shouldThrowWhenNoIpSpec() {
            IpPolicyCreateRequest request = new IpPolicyCreateRequest(
                    "Test Policy", "BLOCKLIST", null, null, null, null, null, null);

            assertThrows(RequestValidationException.class, 
                    () -> ipPolicyService.createPolicy(request, testUser));
        }

        @Test
        @DisplayName("Should create blocklist policy")
        void shouldCreateBlocklistPolicy() {
            IpPolicyCreateRequest request = new IpPolicyCreateRequest(
                    "Block Test", "BLOCKLIST", "192.168.1.100", null, null, null, null, null);

            IpPolicy savedPolicy = new IpPolicy();
            savedPolicy.setId(testPolicyId);
            savedPolicy.setPolicyName("Block Test");
            savedPolicy.setPolicyType(IpPolicy.PolicyType.BLOCKLIST);
            savedPolicy.setIpAddress("192.168.1.100");

            when(ipPolicyRepository.save(any())).thenReturn(savedPolicy);

            var result = ipPolicyService.createPolicy(request, testUser);

            assertNotNull(result);
            assertEquals("Block Test", result.policyName());
            verify(securityEventService).logEventSync(
                    any(SecurityEvent.EventType.class),
                    any(), anyString(), nullable(String.class), nullable(String.class), nullable(String.class),
                    any(), anyInt(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("deletePolicy Tests")
    class DeletePolicyTests {
        @Test
        @DisplayName("Should throw when policy not found")
        void shouldThrowWhenNotFound() {
            when(ipPolicyRepository.findById(testPolicyId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> ipPolicyService.deletePolicy(testPolicyId, testUser));
        }

        @Test
        @DisplayName("Should soft delete policy")
        void shouldDeletePolicy() {
            IpPolicy policy = new IpPolicy();
            policy.setId(testPolicyId);
            policy.setPolicyName("To Delete");

            when(ipPolicyRepository.findById(testPolicyId)).thenReturn(Optional.of(policy));
            when(ipPolicyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ipPolicyService.deletePolicy(testPolicyId, testUser);

            assertTrue(policy.getDeleted());
            assertFalse(policy.isActive());
        }
    }

    @Nested
    @DisplayName("checkAccess Tests")
    class CheckAccessTests {
        @Test
        @DisplayName("Should allow null/empty IP")
        void shouldAllowNullOrEmpty() {
            var result = ipPolicyService.checkAccess(null);
            assertTrue(result.allowed());

            var result2 = ipPolicyService.checkAccess("   ");
            assertTrue(result2.allowed());
        }

        @Test
        @DisplayName("Should block IP in blocklist")
        void shouldBlockIpInBlocklist() {
            IpPolicy blockPolicy = new IpPolicy();
            blockPolicy.setPolicyName("Blocked IP");
            blockPolicy.setPolicyType(IpPolicy.PolicyType.BLOCKLIST);
            blockPolicy.setIpAddress("10.0.0.1");
            blockPolicy.setActive(true);

            when(ipPolicyRepository.findAllActiveBlocklist()).thenReturn(List.of(blockPolicy));

            var result = ipPolicyService.checkAccess("10.0.0.1");

            assertFalse(result.allowed());
            assertEquals("Blocked IP", result.reason());
        }

        @Test
        @DisplayName("Should allow IP not in blocklist")
        void shouldAllowIpNotInBlocklist() {
            when(ipPolicyRepository.findAllActiveBlocklist()).thenReturn(List.of());

            var result = ipPolicyService.checkAccess("192.168.1.1");

            assertTrue(result.allowed());
        }
    }
}