package ir.netpick.mailmine.auth.controller;

import ir.netpick.mailmine.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/{userEmail}/send-verification")
        public ResponseEntity<?> sendVerificationEmail(@PathVariable String userEmail) {
            userService.prepareUserForVerification(userService.getUserEntity(userEmail), true);
            return ResponseEntity.ok().build();
        }
}
