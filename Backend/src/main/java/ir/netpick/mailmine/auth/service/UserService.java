package ir.netpick.mailmine.auth.service;

import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.dto.UserDTO;
import ir.netpick.mailmine.auth.dto.UserUpdateRequest;
import ir.netpick.mailmine.auth.mapper.UserDTOMapper;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.repository.RoleRepository;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.common.constants.GeneralConstants;
import ir.netpick.mailmine.common.enums.RoleEnum;
import ir.netpick.mailmine.common.exception.DuplicateResourceException;
import ir.netpick.mailmine.common.exception.RequestValidationException;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.common.exception.SystemConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

/**
 * Service class for managing users in the authentication system.
 * Provides methods for user creation, retrieval, update, and deletion.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserDTOMapper userDTOMapper;
    private final VerificationService verificationService;

    /**
     * Updates the last sign-in timestamp for a user.
     *
     * @param email the email of the user to update
     */
    @Transactional
    public void updateLastSign(String email) {
        log.debug("Updating last sign-in time for user: {}", email);
        userRepository.updateLastLogin(LocalDateTime.now(), email);
        log.info("Successfully updated last sign-in time for user: {}", email);
    }

    /**
     * Validates if an email address has a proper format.
     *
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public boolean isEmailValidation(String email) {
        log.debug("Validating email format for: {}", email);
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        boolean isValid = Pattern.compile(regexPattern)
                .matcher(email)
                .matches();
        log.debug("Email validation result for {}: {}", email, isValid);
        return isValid;
    }

    /**
     * Validates a registration request for required fields and duplicates.
     *
     * @param request the registration request to validate
     * @return false if the request is valid
     * @throws DuplicateResourceException if a user with the same email already
     *                                    exists
     * @throws RequestValidationException if the email format is invalid or required
     *                                    fields are missing
     */
    public boolean isRegisterRequestInvalid(AuthenticationSignupRequest request) {
        String email = request.email();
        log.debug("Validating registration request for email: {}", email);

        // Check for null fields first before any other validation
        if (email == null || request.password() == null || request.name() == null) {
            log.info("Registration attempt with missing required fields for email: {}", email);
            throw new RequestValidationException(
                    "Please fill in all required fields. " +
                            "Email, name and password are required to create an account.");
        }

        if (userRepository.existsUserByEmail(email)) {
            log.info("Registration attempt with existing email: {}", email);
            throw new DuplicateResourceException(
                    "An account with this email address already exists. " +
                            "Please use a different email or sign in to your existing account.");
        }

        if (!isEmailValidation(email)) {
            log.info("Registration attempt with invalid email format: {}", email);
            throw new RequestValidationException(
                    "The email address you provided is not valid. " +
                            "Please check the format and try again.");
        }

        log.debug("Registration request validation passed for email: {}", email);
        return false;
    }

    /**
     * Retrieves a paginated list of all active (non-deleted) users.
     *
     * @param pageNumber the page number to retrieve (1-indexed)
     * @return a PageDTO containing the users for the requested page
     */
    public PageDTO<UserDTO> allUsers(Integer pageNumber) {
        log.debug("Fetching users page: {}", pageNumber);
        Page<User> page = userRepository
                .findByDeletedFalse(PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                        Sort.by(Sort.Direction.ASC, "createdAt")));

        PageDTO<UserDTO> result = new PageDTO<>(
                page.getContent()
                        .stream()
                        .map(userDTOMapper)
                        .collect(Collectors.toList()),
                page.getTotalPages(),
                page.getNumber() + 1);
        log.info("Successfully fetched {} users for page {}", page.getContent().size(), pageNumber);
        return result;
    }

    /**
     * Retrieves a paginated list of all active (non-deleted) users with custom
     * sorting.
     *
     * @param pageNumber the page number to retrieve (1-indexed)
     * @param sortBy     the field to sort by
     * @param direction  the sort direction (ASC or DESC)
     * @return a PageDTO containing the users for the requested page
     */
    public PageDTO<UserDTO> allUsers(Integer pageNumber, String sortBy, Direction direction) {
        log.debug("Fetching users page: {}, sorted by: {}, direction: {}", pageNumber, sortBy, direction);
        Page<User> page = userRepository
                .findByDeletedFalse(
                        PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE, Sort.by(direction, sortBy)));

        PageDTO<UserDTO> result = new PageDTO<>(
                page.getContent()
                        .stream()
                        .map(userDTOMapper)
                        .collect(Collectors.toList()),
                page.getTotalPages(),
                page.getNumber() + 1);
        log.info("Successfully fetched {} users for page {}, sorted by: {}, direction: {}",
                page.getContent().size(), pageNumber, sortBy, direction);
        return result;
    }

    /**
     * Retrieves a user by their UUID.
     *
     * @param userId the UUID of the user to retrieve
     * @return the UserDTO representing the user
     * @throws ResourceNotFoundException if no user exists with the given UUID
     */
    public UserDTO getUser(UUID userId) {
        log.debug("Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(
                () -> {
                    log.warn("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User with ID [%s] was not found!".formatted(userId));
                });
        log.info("Successfully fetched user with ID: {}", userId);
        return userDTOMapper.apply(user);
    }

    /**
     * Retrieves a user by their email address (excludes soft-deleted users).
     *
     * @param email the email address of the user to retrieve
     * @return the UserDTO representing the user
     * @throws ResourceNotFoundException if no active user exists with the given
     *                                   email
     */
    public UserDTO getUser(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userRepository.findByDeletedFalseAndEmail(email).orElseThrow(
                () -> {
                    log.warn("User not found with email: {}", email);
                    return new ResourceNotFoundException("User with email [%s] was not found!".formatted(email));
                });
        log.info("Successfully fetched user with email: {}", email);
        return userDTOMapper.apply(user);
    }

    /**
     * Retrieves a user entity by their email address (excludes soft-deleted users).
     *
     * @param email the email address of the user to retrieve
     * @return the User entity
     * @throws ResourceNotFoundException if no active user exists with the given
     *                                   email
     */
    public User getUserEntity(String email) {
        log.debug("Fetching user entity by email: {}", email);
        return userRepository.findByDeletedFalseAndEmail(email)
                .orElseThrow(() -> {
                    log.warn("User entity not found with email: {}", email);
                    return new ResourceNotFoundException("User not found");
                });
    }

    /**
     * Retrieves a user entity by their UUID.
     *
     * @param userId the UUID of the user to retrieve
     * @return the User entity
     * @throws ResourceNotFoundException if no user exists with the given UUID
     */
    public User getUserEntity(UUID userId) {
        log.debug("Fetching user entity by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User entity not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });
    }

    /**
     * Creates a new administrator user who is automatically verified.
     *
     * @param request the signup request containing user details
     * @return the created User entity (verified)
     * @throws RequestValidationException   if the request is invalid
     * @throws SystemConfigurationException if the ADMIN role is not found in the
     *                                      system
     */
    public User createAdministrator(AuthenticationSignupRequest request) {
        log.info("Creating new administrator with email: {}", request.email());

        if (isRegisterRequestInvalid(request)) {
            throw new RequestValidationException(
                    "Your registration request is not valid. " +
                            "Please check all fields and try again.");
        }

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);

        if (optionalRole.isEmpty()) {
            log.error("Failed to find ADMIN role");
            throw new SystemConfigurationException("System configuration error: ADMIN role not found");
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                optionalRole.get());

        User savedUser = userRepository.save(user);

        // Automatically verify ADMIN users (but not SUPER_ADMIN)
        savedUser.setIsVerified(true);
        userRepository.save(savedUser);

        log.info("Successfully created administrator with ID: {} and email: {}", savedUser.getId(),
                savedUser.getEmail());
        return savedUser;
    }

    /**
     * Creates a new unverified user.
     *
     * @param request the signup request containing user details
     * @return the created User entity
     * @throws RequestValidationException   if the request is invalid
     * @throws SystemConfigurationException if the USER role is not found in the
     *                                      system
     */
    public User createUnverifiedUser(AuthenticationSignupRequest request) {
        log.info("Creating new unverified user with email: {}", request.email());

        if (isRegisterRequestInvalid(request)) {
            throw new RequestValidationException(
                    "Your registration request is not valid. " +
                            "Please check all fields and try again.");
        }

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            log.error("Failed to find USER role");
            throw new SystemConfigurationException("System configuration error: USER role not found");
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                optionalRole.get());

        User savedUser = userRepository.save(user);
        log.info("Successfully created user with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    /**
     * Prepares a user for verification by creating or updating verification code.
     *
     * @param user The user to prepare for verification
     * @return The verification code that was created/updated
     */
    public String prepareUserForVerification(User user) {
        log.debug("Preparing user for verification. User ID: {}, Email: {}",
                user.getId(), user.getEmail());
        // Delegate to VerificationService
        return verificationService.prepareUserForVerification(user);
    }

    /**
     * Updates a user's information by their email address.
     *
     * @param email   the email address of the user to update
     * @param request the update request containing the new user information
     * @return the updated UserDTO
     * @throws ResourceNotFoundException  if no active user exists with the given
     *                                    email
     * @throws RequestValidationException if no changes were detected in the request
     *                                    or
     */

    public UserDTO updateUser(String email, UserUpdateRequest request) {
        log.info("Updating user with email: {}", email);
        User user = userRepository.findByDeletedFalseAndEmail(email).orElseThrow(
                () -> {
                    log.warn("User not found for update with email: {}", email);
                    return new ResourceNotFoundException(
                            "No user account found with email: " + email + ". " +
                                    "Please check the email address or register for a new account.");
                });

        if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            log.warn("Super Admin cannot be changed.");
            throw new RequestValidationException("Super Admin cannot be changed.");
        }

        boolean changed = false;
        if (request.name() != null && !request.name().equals(user.getName())) {
            log.debug("Updating user name from '{}' to '{}'", user.getName(), request.name());
            user.setName(request.name());
            changed = true;
        }

        if (request.description() != null && !request.description().equals(user.getDescription())) {
            log.debug("Updating user description from '{}' to '{}'", user.getDescription(), request.description());
            user.setDescription(request.description());
            changed = true;
        }

        if (!changed) {
            log.warn("No changes detected for user update: {}", email);
            throw new RequestValidationException(
                    "No changes were detected. " +
                            "Please make sure you've modified at least one field.");
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully updated user with email: {}", email);
        return userDTOMapper.apply(savedUser);
    }

    /**
     * Updates a user's information by their UUID.
     *
     * @param userId  the UUID of the user to update
     * @param request the update request containing the new user information
     * @return the updated UserDTO
     * @throws ResourceNotFoundException  if no user exists with the given UUID
     * @throws RequestValidationException if no changes were detected in the request
     *                                    or if the user is a super admin
     */
    public UserDTO updateUser(UUID userId, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(
                () -> {
                    log.warn("User not found for update with ID: {}", userId);
                    return new ResourceNotFoundException(
                            "No user account found with ID: " + userId + ". " +
                                    "Please check the user ID or contact support if you believe this is an error.");
                });

        if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            log.warn("Super Admin cannot be changed.");
            throw new RequestValidationException("Super Admin cannot be changed.");
        }

        boolean changed = false;
        if (request.name() != null && !request.name().equals(user.getName())) {
            log.debug("Updating user name from '{}' to '{}'", user.getName(), request.name());
            user.setName(request.name());
            changed = true;
        }
        if (request.description() != null && !request.description().equals(user.getDescription())) {
            log.debug("Updating user description from '{}' to '{}'", user.getDescription(), request.description());
            user.setDescription(request.description());
            changed = true;
        }

        if (!changed) {
            log.warn("No changes detected for user update: {}", userId);
            throw new RequestValidationException(
                    "No changes were detected. " +
                            "Please make sure you've modified at least one field.");
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully updated user with ID: {}", userId);
        return userDTOMapper.apply(savedUser);
    }

    /**
     * Soft deletes a user by their email address.
     *
     * @param email the email address of the user to delete
     * @throws ResourceNotFoundException  if no user exists with the given email
     * @throws RequestValidationException if the user is a super admin
     */
    @Transactional
    public void deleteUser(String email) {
        log.info("Soft deleting user with email: {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("User not found for deletion with email: {}", email);
            throw new ResourceNotFoundException(
                    "No user account found with email: " + email + ". " +
                            "Please check the email address or register for a new account.");
        });
        if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            log.warn("Super Admin cannot be changed.");
            throw new RequestValidationException("Super Admin cannot be changed.");
        }
        userRepository.updateDeletedByEmail(true, email);
        log.info("Successfully soft deleted user with email: {}", email);
    }

    /**
     * Soft deletes a user by their UUID.
     *
     * @param userId the UUID of the user to delete
     * @throws ResourceNotFoundException  if no user exists with the given UUID
     * @throws RequestValidationException if the user is a super admin
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Soft deleting user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for deletion with ID: {}", userId);
                    return new ResourceNotFoundException(
                            "No user account found with ID: " + userId + ". " +
                                    "Please check the user ID or contact support if you believe this is an error.");
                });

        if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            log.warn("Super Admin cannot be changed.");
            throw new RequestValidationException("Super Admin cannot be changed.");
        }
        user.setDeleted(true);
        userRepository.save(user);
        log.info("Successfully soft deleted user with ID: {}", userId);
    }

    /**
     * Restores a soft-deleted user by their UUID.
     *
     * @param userId the UUID of the user to restore
     * @throws ResourceNotFoundException  if no user exists with the given UUID
     * @throws RequestValidationException if the user is a super admin
     */
    @Transactional
    public void restoreUser(UUID userId) {
        log.info("Restoring user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for restoration with ID: {}", userId);
                    return new ResourceNotFoundException(
                            "No user account found with ID: " + userId + ".");
                });

        if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            log.warn("Super Admin cannot be changed.");
            throw new RequestValidationException("Super Admin cannot be changed.");
        }
        user.setDeleted(false);
        userRepository.save(user);
        log.info("Successfully restored user with ID: {}", userId);
    }

    /**
     * Changes a user's password.
     *
     * @param email           the email of the user
     * @param currentPassword the current password for verification
     * @param newPassword     the new password to set
     * @throws ResourceNotFoundException  if no user exists with the given email
     * @throws RequestValidationException if the current password is incorrect or
     *                                    the user is super admin
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        log.info("Changing password for user: {}", email);
        User user = userRepository.findByDeletedFalseAndEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for password change: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            log.warn("Super Admin cannot be changed.");
            throw new RequestValidationException("Super Admin cannot be changed.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            log.warn("Invalid current password provided for user: {}", email);
            throw new RequestValidationException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Successfully changed password for user: {}", email);
    }

    /**
     * Permanently deletes a user (admin only).
     *
     * @param userId the UUID of the user to permanently delete
     * @throws ResourceNotFoundException  if no user exists with the given UUID
     * @throws RequestValidationException if the user is a super admin
     */
    @Transactional
    public void permanentlyDeleteUser(UUID userId) {
        log.info("Permanently deleting user with ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User not found for permanent deletion with ID: {}", userId);
            throw new ResourceNotFoundException(
                    "No user account found with ID: " + userId + ".");
        });
        if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            log.warn("Super Admin cannot be deleted.");
            throw new RequestValidationException("Super Admin cannot be deleted.");
        }
        userRepository.deleteById(userId);
        log.info("Successfully permanently deleted user with ID: {}", userId);
    }

}