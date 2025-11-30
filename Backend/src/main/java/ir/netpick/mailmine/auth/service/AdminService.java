package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.email.AuthEmailService;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.common.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;
    private final AuthEmailService authEmailService;

    /**
     * Creates a user account by admin. The user will need email verification.
     * 
     * @param request The signup request details
     * @return The created user (unverified, verification code generated)
     */
    public User createUser(AuthenticationSignupRequest request) {
        User user = userService.createUnverifiedUser(request);

        // Prepare user for verification but don't send email automatically
        // Admin can choose when to send verification email
        userService.prepareUserForVerification(user);

        return user;
    }

    /**
     * Creates an admin account by super admin. Admins are automatically verified.
     * 
     * @param request The signup request details
     * @param role    The role to assign (ADMIN creates verified admin, USER creates
     *                unverified user)
     * @return The created user
     */
    public User createUserWithRole(AuthenticationSignupRequest request, RoleEnum role) {
        User user;
        if (role == RoleEnum.ADMIN) {
            // Admins are auto-verified, no need for verification
            user = userService.createAdministrator(request);
        } else {
            user = userService.createUnverifiedUser(request);
            // Only prepare verification for non-admin users
            userService.prepareUserForVerification(user);
        }

        return user;
    }

    /**
     * Sends verification email to a user created by admin
     * 
     * @param userEmail The email of the user to send verification to
     */
    public void sendVerificationEmailToUser(String userEmail) {
        // Verify user exists before sending email
        userService.getUserEntity(userEmail);
        authEmailService.sendVerificationEmail(userEmail);
    }
}