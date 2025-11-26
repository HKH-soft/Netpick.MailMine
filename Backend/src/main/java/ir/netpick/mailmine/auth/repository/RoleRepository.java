package ir.netpick.mailmine.auth.repository;

import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.common.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleEnum name);
}
