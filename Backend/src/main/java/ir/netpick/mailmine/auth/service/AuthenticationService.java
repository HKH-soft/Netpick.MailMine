package ir.netpick.mailmine.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import ir.netpick.mailmine.auth.dto.AuthenticationSigninRequest;
import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.dto.UserDTO;
import ir.netpick.mailmine.auth.jwt.JWTUtil;
import ir.netpick.mailmine.auth.mapper.UserDTOMapper;
import ir.netpick.mailmine.auth.model.User;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserDTOMapper userDTOMapper;
    private final UserService userService;

    public AuthenticationService(AuthenticationManager authenticationManager, JWTUtil jwtUtil,
            UserDTOMapper userDTOMapper, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDTOMapper = userDTOMapper;
        this.userService = userService;
    }

    public String signin(AuthenticationSigninRequest request) {
        Authentication authenticationRsponse = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDTO user = userDTOMapper.apply((User) authenticationRsponse.getPrincipal());
        String token = jwtUtil.issueToken(user.email(), user.role().toString());
        userService.updateLastSign(request.email());
        return token;
    }

    public String signup(AuthenticationSignupRequest request) {
        User user = userService.createUser(request);
        return jwtUtil.issueToken(request.email(), user.getRole().getName().toString());
    }

    public String registerAdmin(AuthenticationSignupRequest request) {
        User user = userService.createAdministrator(request);
        return jwtUtil.issueToken(request.email(), user.getRole().getName().toString());
    }

}
