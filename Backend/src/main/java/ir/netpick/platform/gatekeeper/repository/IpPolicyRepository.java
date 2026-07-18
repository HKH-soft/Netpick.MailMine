package ir.netpick.platform.gatekeeper.repository;

import ir.netpick.platform.gatekeeper.model.IpPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IpPolicyRepository extends JpaRepository<IpPolicy, UUID> {

    List<IpPolicy> findByPolicyTypeAndActiveTrue(IpPolicy.PolicyType policyType);

    @Query("SELECT ip FROM IpPolicy ip WHERE ip.active = true AND (ip.policyType = 'BLOCKLIST')")
    List<IpPolicy> findAllActiveBlocklist();

    @Query("SELECT ip FROM IpPolicy ip WHERE ip.active = true AND (ip.policyType = 'ALLOWLIST')")
    List<IpPolicy> findAllActiveAllowlist();

    boolean existsByIpAddressAndPolicyTypeAndActiveTrue(String ipAddress, IpPolicy.PolicyType policyType);

    @Query("SELECT ip FROM IpPolicy ip WHERE ip.ipAddress = :ip AND ip.active = true")
    List<IpPolicy> findByIpAddress(String ip);
}
