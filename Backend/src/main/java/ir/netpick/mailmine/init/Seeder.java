package ir.netpick.mailmine.init;

import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.repository.RoleRepository;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.common.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Seeder implements ApplicationListener<ContextRefreshedEvent> {
  private static final Logger logger = LogManager.getLogger(Seeder.class);

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  // private final ScrapeService scrapeService;
  // private final Scrape scrape;

  private final PasswordEncoder passwordEncoder;

  @Override
  public void onApplicationEvent(@NotNull ContextRefreshedEvent contextRefreshedEvent) {
    this.loadRoles();
    this.createSuperAdmin();
    // this.webScrape();
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
    
    // Mark super admin as verified by default
    user.setIsVerified(true);

    userRepository.save(user);
    logger.info("superuser was created");
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

  // private void webScrape() {
  // if (!scrapeService.scrapeJobExists("https://netpick.ir")) {
  // ScrapeJob job = new ScrapeJob("https://netpick.ir", "test");
  // scrapeService.createScrapeJob(job);
  // }
  // if
  // (!scrapeService.scrapeJobExists("https://en.wikipedia.org/wiki/Main_Page")) {
  // ScrapeJob job = new ScrapeJob("https://en.wikipedia.org/wiki/Main_Page",
  // "test");
  // scrapeService.createScrapeJob(job);
  // }
  // logger.info(scrape.getDataCount());
  // scrape.webGet(); // yeahhhhhh it workssssss
  // }
}