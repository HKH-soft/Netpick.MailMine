package ir.netpick.platform.gatekeeper.repository;

import ir.netpick.platform.gatekeeper.model.Role;
import ir.netpick.platform.core.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleEnum name);
}









