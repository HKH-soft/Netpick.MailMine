package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.dto.AuthenticationSigninRequest;
import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.dto.UserDTO;
import ir.netpick.mailmine.auth.dto.VerificationRequest;
import ir.netpick.mailmine.auth.email.EmailServiceImpl;
import ir.netpick.mailmine.auth.jwt.JWTUtil;
import ir.netpick.mailmine.auth.mapper.UserDTOMapper;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.model.Verification;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.common.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import static ir.netpick.mailmine.common.enums.RoleEnum.USER;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserDTOMapper userDTOMapper;
    private final UserService userService;
    private final UserRepository userRepository;
    private final VerificationService verificationService;
    private final EmailServiceImpl emailService;

    public String signIn(AuthenticationSigninRequest request) {
        Authentication authenticationResponse = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDTO user = userDTOMapper.apply((User) authenticationResponse.getPrincipal());
        
        // Check if user is verified (except for SUPER_ADMIN users)
        if (!user.isVerified() && user.role() != RoleEnum.SUPER_ADMIN) {
            throw new IllegalStateException("Account not verified. Please verify your email.");
        }
        
        String token = jwtUtil.issueToken(user.email(), user.role().toString());
        userService.updateLastSign(request.email());
        return token;
    }

    public void signUp(AuthenticationSignupRequest request) {
        User user = userService.createUnverifiedUser(request);
        
        // Prepare user for verification and send email
        userService.prepareUserForVerification(user, true);
        emailService.sendVerificationEmail(user.getEmail());
    }

    public void verifyUser(VerificationRequest request) {
        User user = userService.getUserEntity(request.email());

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (user.getVerification() == null) {
            throw new IllegalStateException("No verification code found for this user");
        }

        verificationService.verifyCode(user.getVerification(), request.code());

        user.setIsVerified(true);
        user.setVerification(null);
        userRepository.save(user);
    }
    
    public void resendVerification(String email) {
        User user = userService.getUserEntity(email);
        
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new IllegalStateException("User is already verified");
        }
        
        // Update verification with new code
        userService.prepareUserForVerification(user, true);
        emailService.sendVerificationEmail(email);
    }

}