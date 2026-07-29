package ir.netpick.platform.init;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

  @PersistenceContext
  private EntityManager entityManager;

  private boolean executed = false;

  @Override
  @Transactional
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
   * Uses native query to avoid JpaObjectRetrievalFailureException from eager FK loading.
   */
  private void recoverOrphanedUsers() {
    List<Role> validRoles = roleRepository.findAll();
    if (validRoles.isEmpty()) {
      return;
    }

    Set<UUID> validRoleIds = validRoles.stream()
        .map(Role::getId)
        .collect(Collectors.toSet());

    // Use native query to find orphaned users without triggering eager FK loading
    Query orphanedQuery = entityManager.createNativeQuery(
        "SELECT id FROM users WHERE role_id IS NULL OR role_id NOT IN (" +
        validRoleIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")"
    );
    int i = 1;
    for (UUID id : validRoleIds) {
      orphanedQuery.setParameter(i++, id);
    }

    @SuppressWarnings("unchecked")
    List<Object> orphanedUserIds = orphanedQuery.getResultList();

    if (orphanedUserIds.isEmpty()) {
      return;
    }

    log.warn("Found {} user(s) with invalid role references (soft-deleting)", orphanedUserIds.size());
    // Use native UPDATE for soft-delete to avoid EntityNotFoundException:
    // deleteById() loads the entity first, triggering EAGER fetch of the broken role FK
    Query softDeleteQuery = entityManager.createNativeQuery(
        "UPDATE users SET deleted = true WHERE id IN (" +
        orphanedUserIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(",")) + ")"
    );
    softDeleteQuery.executeUpdate();
  }

  private void createSuperAdmin() {
    String email = "super.admin@netpick.ir";

    Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);
    if (optionalRole.isEmpty()) {
      log.error("SUPER_ADMIN role not found — cannot create super admin");
      return;
    }

    String password = System.getenv("SUPER_ADMIN_PASSWORD");
    if (password == null || password.isBlank()) {
      password = java.util.UUID.randomUUID().toString();
      log.warn("SUPER_ADMIN_PASSWORD not set — generated random password — Password: " + password);
    }

    String encodedPassword = passwordEncoder.encode(password);

    Optional<User> existingUser = userRepository.findByDeletedFalseAndEmail(email);
    if (existingUser.isPresent()) {
      User user = existingUser.get();
      user.setPasswordHash(encodedPassword);
      user.setRole(optionalRole.get());
      user.setIsVerified(true);
      userRepository.save(user);
      log.info("Super admin user password updated");
      return;
    }

    Optional<User> softDeletedUser = userRepository.findByDeletedTrueAndEmail(email);
    if (softDeletedUser.isPresent()) {
      User user = softDeletedUser.get();
      user.setPasswordHash(encodedPassword);
      user.setRole(optionalRole.get());
      user.setIsVerified(true);
      user.setDeleted(false);
      userRepository.save(user);
      log.info("Super admin user was recovered and password updated");
      return;
    }

    User user = new User(email, encodedPassword, "superAdmin",
        optionalRole.get());
    user.setIsVerified(true);
    userRepository.save(user);
    log.info("Super admin user was created");
  }
}