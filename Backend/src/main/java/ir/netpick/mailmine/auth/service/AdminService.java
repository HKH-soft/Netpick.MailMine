package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.email.EmailService;
import ir.netpick.mailmine.auth.email.EmailServiceImpl;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.common.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;
    private final EmailService emailService;

    /**
     * Creates a user account by admin without immediate verification requirement
     * @param request The signup request details
     * @return The created user
     */
    public User createAdminUser(AuthenticationSignupRequest request) {
        User user = userService.createUnverifiedUser(request);
        
        // Prepare user for verification but don't send email automatically
        // Admin can choose when to send verification email
        userService.prepareUserForVerification(user, false);
        
        return user;
    }
    
    /**
     * Creates an admin account by super admin without verification requirement
     * @param request The signup request details
     * @param role The role to assign to the user
     * @return The created user
     */
    public User createAdminUser(AuthenticationSignupRequest request, RoleEnum role) {
        User user;
        if (role == RoleEnum.ADMIN) {
            user = userService.createUnverifiedAdministrator(request);
        } else {
            user = userService.createUnverifiedUser(request);
        }
        
        // For admin roles, we might want to auto-verify them or leave them unverified
        // depending on security requirements. For now, we'll prepare verification but not send email.
        userService.prepareUserForVerification(user, false);
        
        return user;
    }
    
    /**
     * Sends verification email to a user created by admin
     * @param userEmail The email of the user to send verification to
     */
    public void sendVerificationEmailToUser(String userEmail) {
        User user = userService.getUserEntity(userEmail);
        emailService.sendVerificationEmail(userEmail);
    }
}