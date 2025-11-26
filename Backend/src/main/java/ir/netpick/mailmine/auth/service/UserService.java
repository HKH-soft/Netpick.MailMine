package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.dto.UserDTO;
import ir.netpick.mailmine.auth.dto.UserUpdateRequest;
import ir.netpick.mailmine.auth.mapper.UserDTOMapper;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.model.Verification;
import ir.netpick.mailmine.auth.repository.RoleRepository;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.common.constants.GeneralConstants;
import ir.netpick.mailmine.common.enums.RoleEnum;
import ir.netpick.mailmine.common.exception.DuplicateResourceException;
import ir.netpick.mailmine.common.exception.RequestValidationException;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.devtools.RequestFailedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserDTOMapper userDTOMapper;
    private final VerificationService verificationService;


    public void setVerificationFor(User user, Verification verification){
        user.setVerification(verification);
        userRepository.save(user);
    }
    
    @Transactional
    public void updateLastSign(String email) {
        userRepository.updateLastLogin(LocalDateTime.now(), email);
    }
    
    public boolean isEmailValidation(String email) {
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(regexPattern)
                .matcher(email)
                .matches();
    }
    
    public boolean isRegisterRequestInvalid(AuthenticationSignupRequest request) {
        String email = request.email();
        if (userRepository.existsUserByEmail(email)) {
            throw new DuplicateResourceException("A User with this email already exists.");
        }
        if (!isEmailValidation(email)) {
            throw new RequestValidationException("Email is not Valid.");
        }
        if (request.password() == null ||
                request.name() == null) {
            throw new RequestValidationException("There is an empty parameter.");
        }
        return false;
    }
    
    public PageDTO<UserDTO> allUsers(Integer pageNumber) {
        Page<User> page = userRepository
                .findAll(PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE, Sort.by(Sort.Direction.ASC, "createdAt")));

        return new PageDTO<>(
                page.getContent()
                        .stream()
                        .map(userDTOMapper)
                        .collect(Collectors.toList()),
                page.getTotalPages(),
                page.getNumber() + 1);
    }
    
    public PageDTO<UserDTO> allUsers(Integer pageNumber, String sortBy, Direction direction) {
        Page<User> page = userRepository.findAll(PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE, Sort.by(direction, sortBy)));

        return new PageDTO<>(
                page.getContent()
                        .stream()
                        .map(userDTOMapper)
                        .collect(Collectors.toList()),
                page.getTotalPages(),
                page.getNumber() + 1);
    }
    
    public UserDTO getUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User with email [%s] was not found!".formatted(userId)));
        return userDTOMapper.apply(user);
    }
    
    public UserDTO getUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User with email [%s] was not found!".formatted(email)));
        return userDTOMapper.apply(user);
    }
    
    public User getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    public User getUserEntity(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    public User createUnverifiedAdministrator(AuthenticationSignupRequest request) {

        if (isRegisterRequestInvalid(request)) {
            throw new RequestValidationException("Request is not valid!");
        }

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);

        if (optionalRole.isEmpty()) {
            throw new RequestFailedException();
        }

        User user = new User(request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                optionalRole.get());

        return userRepository.save(user);
    }
    
    public User createUnverifiedUser(AuthenticationSignupRequest request) {

        if (isRegisterRequestInvalid(request)) {
            throw new RequestValidationException("Request is not valid!");
        }

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            throw new RequestFailedException();
        }

        User user =new User(request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                optionalRole.get());

        return userRepository.save(user);
    }

    /**
     * Prepares a user for verification by creating or updating verification code
     * @param user The user to prepare for verification
     * @param sendEmail Whether to send verification email
     * @return The verification code that was created/updated
     */
    public String prepareUserForVerification(User user, boolean sendEmail) {
        // If user already verified, no need to send verification
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new IllegalStateException("User is already verified");
        }

        String verificationCode;
        // Create or update verification code
        if (user.getVerification() == null) {
            Verification verification = verificationService.createVerification();
            setVerificationFor(user, verification);
            verificationCode = verification.getCode();
        } else {
            // Update with new code
            verificationService.updateVerification(user.getVerification());
            verificationCode = user.getVerification().getCode();
            
            // Save the updated user
            userRepository.save(user);
        }
        
        return verificationCode;
    }

    public UserDTO updateUser(String email , UserUpdateRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User with email [%s] was not found!".formatted(email)));
        boolean changed = false;
        if (request.name() != null && !request.name().equals(user.getName())) {
            user.setName(request.name());
            changed = true;
        }
        if (request.description() != null && !request.description().equals(user.getDescription())) {
            user.setDescription(request.description());
            changed = true;
        }

        if(!changed){
            throw new RequestValidationException("There were no changes");
        }
        return userDTOMapper.apply(userRepository.save(user));

    }
    
    public UserDTO updateUser(UUID userId , UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User with email [%s] was not found!".formatted(userId)));
        boolean changed = false;
        if(request.name() != null && !request.name().equals(user.getName())) {
            user.setName(request.name());
            changed = true;
        }
        if (request.description() != null && !request.description().equals(user.getDescription())) {
            user.setDescription(request.description());
            changed = true;
        }

        if(!changed){
            throw new RequestValidationException("There were no changes");
        }

        return userDTOMapper.apply(userRepository.save(user));

    }
    
    public void deleteUser(String email) {
        if (!userRepository.existsUserByEmail(email)) {
            throw new ResourceNotFoundException("User with email [%s] was not found!".formatted(email));
        }
        userRepository.deleteByEmail(email);
    }
    
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with email [%s] was not found!".formatted(userId));
        }
        userRepository.deleteById(userId);
    }

}