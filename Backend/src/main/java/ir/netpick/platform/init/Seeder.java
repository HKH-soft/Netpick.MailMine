package ir.netpick.platform.init;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ir.netpick.platform.gatekeeper.model.Role;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.RoleRepository;
import ir.netpick.platform.gatekeeper.repository.UserRepository;
import ir.netpick.platform.core.enums.RoleEnum;
import lombok.RequiredArgsConstructor;

@Slf4j
@Component
@RequiredArgsConstructor
public class Seeder implements ApplicationListener<ContextRefreshedEvent> {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  private boolean executed = false;

  @Override
  public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
    if (executed) {
      return;
    }
    executed = true;

    this.loadRoles();
    this.recoverOrphanedUsers();
    this.createSuperAdmin();
  }

  private void loadRoles() {
    RoleEnum[] roleNames = new RoleEnum[] { RoleEnum.USER, RoleEnum.ADMIN, RoleEnum.SUPER_ADMIN };
    Map<RoleEnum, String> roleDescriptionMap = Map.of(
        RoleEnum.USER, "Default user role",
        RoleEnum.ADMIN, "Administrator role",
        RoleEnum.SUPER_ADMIN, "Super Administrator role");

    Arrays.stream(roleNames).forEach((roleName) -> {
      Optional<Role> optionalRole = roleRepository.findByName(roleName);
      optionalRole.ifPresentOrElse(
          role -> log.debug("Role '{}' already exists with id {}", roleName, role.getId()),
          () -> {
            Role roleToCreate = new Role();
            roleToCreate.setName(roleName);
            roleToCreate.setDescription(roleDescriptionMap.get(roleName));
            roleRepository.save(roleToCreate);
            log.info("Created role '{}'", roleName);
          });
    });
  }

  /**
   * Removes users whose role_id references a non-existent Role.
   * Prevents JpaObjectRetrievalFailureException from eager FK loading.
   */
  private void recoverOrphanedUsers() {
    List<Role> validRoles = roleRepository.findAll();
    if (validRoles.isEmpty()) {
      return;
    }

    Set<UUID> validRoleIds = validRoles.stream()
        .map(Role::getId)
        .collect(Collectors.toSet());

    List<User> orphanedUsers = userRepository.findAll().stream()
        .filter(user -> user.getRole() == null || !validRoleIds.contains(user.getRole().getId()))
        .toList();

if (orphanedUsers.isEmpty()) {
       return;
     }

     log.warn("Found {} user(s) with invalid role references (not deleting)", orphanedUsers.size());
     for (User orphan : orphanedUsers) {
       log.warn("Orphaned user: email={}", orphan.getEmail());
     }
   }

  private void createSuperAdmin() {
    String email = "super.admin@netpick.ir";

    Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);
    if (optionalRole.isEmpty()) {
      log.error("SUPER_ADMIN role not found — cannot create super admin");
      return;
    }

    // existsUserByEmail avoids eager-loading the User entity (and its FK to Role).
    if (userRepository.existsUserByEmail(email)) {
      log.debug("Super admin user already exists — skipping creation");
      return;
    }

    String password = System.getenv("SUPER_ADMIN_PASSWORD");
    if (password == null || password.isBlank()) {
      password = java.util.UUID.randomUUID().toString();
      log.warn("SUPER_ADMIN_PASSWORD not set — generated random password");
    }
    User user = new User(email, passwordEncoder.encode(password), "superAdmin",
        optionalRole.get());
    user.setIsVerified(true);
    userRepository.save(user);
    log.info("superuser was created");
  }
}
