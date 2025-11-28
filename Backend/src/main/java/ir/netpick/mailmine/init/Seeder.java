package ir.netpick.mailmine.init;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.repository.RoleRepository;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.common.enums.RoleEnum;
import lombok.RequiredArgsConstructor;

@Slf4j
@Component
@RequiredArgsConstructor
public class Seeder implements ApplicationListener<ContextRefreshedEvent> {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  @Override
  public void onApplicationEvent(@NotNull ContextRefreshedEvent contextRefreshedEvent) {
    this.loadRoles();
    this.createSuperAdmin();
  }

  private void createSuperAdmin() {
    AuthenticationSignupRequest request = new AuthenticationSignupRequest(
        "super.admin@netpick.ir",
        "password",
        "superAdmin");
    Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);
    Optional<User> optionalUser = userRepository.findByEmail(request.email());

    if (optionalRole.isEmpty() || optionalUser.isPresent()) {
      return;
    }

    User user = new User(request.email(), passwordEncoder.encode(request.password()), request.name(),
        optionalRole.get());

    userRepository.save(user);
    log.info("superuser was created");
  }

  private void loadRoles() {
    RoleEnum[] roleNames = new RoleEnum[] { RoleEnum.USER, RoleEnum.ADMIN, RoleEnum.SUPER_ADMIN };
    Map<RoleEnum, String> roleDescriptionMap = Map.of(
        RoleEnum.USER, "Default user role",
        RoleEnum.ADMIN, "Administrator role",
        RoleEnum.SUPER_ADMIN, "Super Administrator role");

    Arrays.stream(roleNames).forEach((roleName) -> {
      Optional<Role> optionalRole = roleRepository.findByName(roleName);

      optionalRole.ifPresentOrElse(System.out::println, () -> {
        Role roleToCreate = new Role();

        roleToCreate.setName(roleName);
        roleToCreate.setDescription(roleDescriptionMap.get(roleName));

        roleRepository.save(roleToCreate);
      });

    });
  }
}