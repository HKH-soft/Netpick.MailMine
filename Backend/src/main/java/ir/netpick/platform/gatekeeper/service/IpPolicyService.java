package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.dto.IpPolicyCreateRequest;
import ir.netpick.platform.gatekeeper.dto.IpPolicyDTO;
import ir.netpick.platform.gatekeeper.model.IpPolicy;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.IpPolicyRepository;
import ir.netpick.platform.core.exception.RequestValidationException;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IpPolicyService {

    private final IpPolicyRepository ipPolicyRepository;
    private final SecurityEventService securityEventService;

    @Transactional
    public IpPolicyDTO createPolicy(IpPolicyCreateRequest request, User createdBy) {
        IpPolicy.PolicyType type;
        try {
            type = IpPolicy.PolicyType.valueOf(request.policyType());
        } catch (IllegalArgumentException e) {
            throw new RequestValidationException("Invalid policy type. Must be ALLOWLIST or BLOCKLIST.");
        }

        if (request.ipAddress() == null && request.ipRangeStart() == null && request.cidrNotation() == null) {
            throw new RequestValidationException("At least one IP specification is required (ipAddress, ipRange, or cidrNotation).");
        }

        IpPolicy policy = new IpPolicy();
        policy.setPolicyName(request.policyName());
        policy.setPolicyType(type);
        policy.setIpAddress(request.ipAddress());
        policy.setIpRangeStart(request.ipRangeStart());
        policy.setIpRangeEnd(request.ipRangeEnd());
        policy.setCidrNotation(request.cidrNotation());
        policy.setDescription(request.description());
        policy.setExpiresAt(request.expiresAt());
        policy.setCreatedBy(createdBy);
        policy.setActive(true);

        IpPolicy saved = ipPolicyRepository.save(policy);

        securityEventService.logEventSync(
                ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.IP_POLICY_CREATED,
                createdBy.getId(), createdBy.getEmail(), null, null, null,
                java.util.Map.of("policyId", saved.getId().toString(), "policyType", type.name(),
                        "policyName", request.policyName()),
                0, false);

        log.info("IP policy created: {} type={} by {}", request.policyName(), type, createdBy.getEmail());
        return toDto(saved);
    }

    @Transactional
    public void deletePolicy(UUID policyId, User deletedBy) {
        IpPolicy policy = ipPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("IP policy not found"));

        policy.setDeleted(true);
        policy.setActive(false);
        ipPolicyRepository.save(policy);

        securityEventService.logEventSync(
                ir.netpick.platform.gatekeeper.model.SecurityEvent.EventType.IP_POLICY_DELETED,
                deletedBy.getId(), deletedBy.getEmail(), null, null, null,
                java.util.Map.of("policyId", policyId.toString()), 0, false);

        log.info("IP policy deleted: {} by {}", policy.getPolicyName(), deletedBy.getEmail());
    }

    public List<IpPolicyDTO> getAllPolicies() {
        return ipPolicyRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .map(this::toDto)
                .toList();
    }

    public List<IpPolicyDTO> getPoliciesByType(String type) {
        IpPolicy.PolicyType policyType;
        try {
            policyType = IpPolicy.PolicyType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new RequestValidationException("Invalid policy type.");
        }
        return ipPolicyRepository.findByPolicyTypeAndActiveTrue(policyType).stream()
                .map(this::toDto)
                .toList();
    }

    public IpAccessResult checkAccess(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return new IpAccessResult(true, null);
        }

        List<IpPolicy> blocklist = ipPolicyRepository.findAllActiveBlocklist();
        for (IpPolicy policy : blocklist) {
            if (policyMatches(policy, ipAddress)) {
                if (policy.getExpiresAt() != null && policy.getExpiresAt().isBefore(Instant.now())) {
                    continue;
                }
                return new IpAccessResult(false, policy.getPolicyName());
            }
        }

        List<IpPolicy> allowlist = ipPolicyRepository.findAllActiveAllowlist();
        if (!allowlist.isEmpty()) {
            boolean allowed = false;
            for (IpPolicy policy : allowlist) {
                if (policyMatches(policy, ipAddress)) {
                    if (policy.getExpiresAt() != null && policy.getExpiresAt().isBefore(Instant.now())) {
                        continue;
                    }
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                return new IpAccessResult(false, "IP not in allowlist");
            }
        }

        return new IpAccessResult(true, null);
    }

    private boolean policyMatches(IpPolicy policy, String ipAddress) {
        if (policy.getIpAddress() != null && policy.getIpAddress().equals(ipAddress)) {
            return true;
        }

        if (policy.getCidrNotation() != null) {
            return ipMatchesCidr(ipAddress, policy.getCidrNotation());
        }

        if (policy.getIpRangeStart() != null && policy.getIpRangeEnd() != null) {
            return ipInRange(ipAddress, policy.getIpRangeStart(), policy.getIpRangeEnd());
        }

        return false;
    }

    private boolean ipMatchesCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String networkAddress = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            long ipLong = ipToLong(InetAddress.getByName(ip));
            long networkLong = ipToLong(InetAddress.getByName(networkAddress));
            long mask = -1L << (32 - prefixLength);

            return (ipLong & mask) == (networkLong & mask);
        } catch (Exception e) {
            log.warn("Failed to parse CIDR: {}", cidr);
            return false;
        }
    }

    private boolean ipInRange(String ip, String start, String end) {
        try {
            long ipLong = ipToLong(InetAddress.getByName(ip));
            long startLong = ipToLong(InetAddress.getByName(start));
            long endLong = ipToLong(InetAddress.getByName(end));
            return ipLong >= startLong && ipLong <= endLong;
        } catch (Exception e) {
            log.warn("Failed to check IP range: {} - {}", start, end);
            return false;
        }
    }

    private long ipToLong(InetAddress address) {
        byte[] octets = address.getAddress();
        return ((long) (octets[0] & 0xFF) << 24)
                | ((long) (octets[1] & 0xFF) << 16)
                | ((long) (octets[2] & 0xFF) << 8)
                | (octets[3] & 0xFF);
    }

    private IpPolicyDTO toDto(IpPolicy policy) {
        return new IpPolicyDTO(
                policy.getId(),
                policy.getPolicyName(),
                policy.getPolicyType().name(),
                policy.getIpAddress(),
                policy.getIpRangeStart(),
                policy.getIpRangeEnd(),
                policy.getCidrNotation(),
                policy.getDescription(),
                policy.isActive(),
                policy.getExpiresAt(),
                policy.getCreatedBy() != null ? policy.getCreatedBy().getEmail() : null,
                policy.getCreatedAt());
    }

    public record IpAccessResult(boolean allowed, String reason) {}
}
