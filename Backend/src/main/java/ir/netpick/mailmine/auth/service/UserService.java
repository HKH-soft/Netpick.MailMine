package ir.netpick.mailmine.auth.service;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import ir.netpick.mailmine.auth.dto.AuthenticationSignupRequest;
import ir.netpick.mailmine.auth.dto.UserDTO;
import ir.netpick.mailmine.auth.dto.UserUpdateRequest;
import ir.netpick.mailmine.auth.mapper.UserDTOMapper;
import ir.netpick.mailmine.auth.model.Role;
import ir.netpick.mailmine.auth.model.User;
import ir.netpick.mailmine.auth.repository.RoleRepository;
import ir.netpick.mailmine.auth.repository.UserRepository;
import ir.netpick.mailmine.common.PageDTO;
import ir.netpick.mailmine.common.result.Result;
import ir.netpick.mailmine.common.result.error.Error;
import ir.netpick.mailmine.common.result.success.Success;
import ir.netpick.mailmine.common.constants.GeneralConstants;
import ir.netpick.mailmine.common.enums.RoleEnum;
import ir.netpick.mailmine.common.exception.DuplicateResourceException;
import ir.netpick.mailmine.common.exception.RequestValidationException;
import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.common.exception.SystemConfigurationException;
import ir.netpick.mailmine.common.utils.PageDTOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class for managing users in the authentication system.
 * Provides methods for user creation, retrieval, update, and deletion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserDTOMapper userDTOMapper;
    private final VerificationService verificationService;

    // Precompiled patterns for efficiency
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*\\p{Ll}.*");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*\\p{Lu}.*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[^\\p{L}\\d].*");

    // Leet speak substitutions for better password checking
    private static final Map<Character, Character> LEET_SPEAK_MAP = Map.of(
            '0', 'o', '1', 'i', '3', 'e', '4', 'a', '5', 's', '7', 't', '8', 'b', '@', 'a', '$', 's');

    // Common passwords loaded from file
    private static final Set<String> COMMON_PASSWORDS = loadCommonPasswords();

    // zxcvbn instance for password strength checking
    private static final Zxcvbn ZXCVBN = new Zxcvbn();

    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MIN_CATEGORY_COUNT = 3;
    private static final int MIN_ZXCVBN_SCORE = 3;

    /**
     * Load common passwords from classpath resource file.
     */
    private static Set<String> loadCommonPasswords() {
        try {
            ClassPathResource resource = new ClassPathResource("common-passwords.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.toSet());
            }
        } catch (IOException e) {
            log.error("Failed to load common passwords file. Using minimal set.", e);
            return Set.of("password", "123456", "qwerty", "abc123", "letmein");
        }
    }

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
        boolean isValid = EMAIL_PATTERN.matcher(email).matches();
        log.debug("Email validation result for {}: {}", email, isValid);
        return isValid;
    }

    /**
     * Normalizes password to detect leet-speak and common substitutions.
     *
     * @param password the password to normalize
     * @return normalized password
     */
    private String normalizeLeetSpeak(String password) {
        StringBuilder normalized = new StringBuilder();
        for (char c : password.toLowerCase().toCharArray()) {
            normalized.append(LEET_SPEAK_MAP.getOrDefault(c, c));
        }
        return normalized.toString();
    }

    /**
     * Validates password strength with detailed feedback.
     * Returns a Result with specific error codes and messages for better UX.
     *
     * @param password the password to validate
     * @param email    the user's email (optional, for checking personal info)
     * @param name     the user's name (optional, for checking personal info)
     * @return Result<Success> with success or detailed errors
     */
    public Result<Success> validatePassword(String password, String email, String name) {
        List<Error> errors = new ArrayList<>();

        // 1. Null or empty check
        if (password == null || password.trim().isEmpty()) {
            return Result.error(new Error("Password.REQUIRED", "Password is required"));
        }

        // 2. Length check
        if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.add(new Error("Password.TOO_SHORT",
                    String.format("Password must be at least %d characters long", MIN_PASSWORD_LENGTH)));
        }

        // 3. Character variety check (using Unicode-aware patterns)
        int categories = 0;
        if (LOWERCASE_PATTERN.matcher(password).matches())
            categories++;
        if (UPPERCASE_PATTERN.matcher(password).matches())
            categories++;
        if (DIGIT_PATTERN.matcher(password).matches())
            categories++;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches())
            categories++;

        if (categories < MIN_CATEGORY_COUNT) {
            errors.add(new Error("Password.WEAK_VARIETY",
                    String.format(
                            "Password must contain at least %d of: lowercase, uppercase, numbers, special characters",
                            MIN_CATEGORY_COUNT)));
        }

        // 4. Personal information check (email local-part)
        if (email != null && !email.trim().isEmpty()) {
            try {
                int atIndex = email.indexOf('@');
                if (atIndex > 0) {
                    String localPart = email.substring(0, atIndex).toLowerCase();
                    String passwordLower = password.toLowerCase();
                    String normalizedPassword = normalizeLeetSpeak(password);

                    if (localPart.length() >= 3 && (passwordLower.contains(localPart)
                            || normalizedPassword.contains(localPart))) {
                        errors.add(new Error("Password.CONTAINS_EMAIL",
                                "Password must not contain your email address"));
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to validate email local-part in password check", e);
            }
        }

        // 5. Personal information check (name)
        if (name != null && !name.trim().isEmpty()) {
            String nameLower = name.toLowerCase().replaceAll("\\s+", "");
            String passwordLower = password.toLowerCase();
            String normalizedPassword = normalizeLeetSpeak(password);

            if (nameLower.length() >= 3 && (passwordLower.contains(nameLower)
                    || normalizedPassword.contains(nameLower))) {
                errors.add(new Error("Password.CONTAINS_NAME", "Password must not contain your name"));
            }
        }

        // 6. Common passwords check
        String passwordLower = password.toLowerCase();
        if (COMMON_PASSWORDS.contains(passwordLower)) {
            errors.add(new Error("Password.TOO_COMMON",
                    "Password is too common. Please choose a more unique password"));
        }

        // 7. zxcvbn strength analysis (entropy-based)
        try {
            List<String> userInputs = new ArrayList<>();
            if (email != null)
                userInputs.add(email);
            if (name != null)
                userInputs.add(name);

            Strength strength = ZXCVBN.measure(password, userInputs);
            if (strength.getScore() < MIN_ZXCVBN_SCORE) {
                String feedback = strength.getFeedback() != null && strength.getFeedback().getWarning() != null
                        ? strength.getFeedback().getWarning()
                        : "Password is too weak. Add more words or use longer phrases";
                errors.add(new Error("Password.TOO_WEAK", feedback));
            }
        } catch (Exception e) {
            log.warn("zxcvbn password strength check failed, skipping", e);
        }

        return errors.isEmpty() ? Result.ok() : Result.errors(errors);
    }

    /**
     * Converts an email address to the corresponding user ID.
     *
     * @param email the email address of the user
     * @return the UUID of the user associated with the given email
     */
    private UUID emailToUserId(String email) {
        log.debug("Converting email to user ID for email: {}", email);
        return userRepository.findIdByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
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
        return allUsers(pageNumber, "createdAt", Direction.ASC);
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

        PageDTO<UserDTO> result = PageDTOMapper.map(page, userDTOMapper);
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
        UUID userId = emailToUserId(email);
        return getUser(userId);
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
        UUID userId = emailToUserId(email);
        return getUserEntity(userId);
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
        return createUser(request, roleRepository.findByName(RoleEnum.ADMIN), true);
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
        return createUser(request, roleRepository.findByName(RoleEnum.USER), false);
    }

    private User createUser(AuthenticationSignupRequest request, Optional<Role> optionalRole, boolean verified) {
        log.info("Creating new unverified user with email: {}", request.email());

        if (isRegisterRequestInvalid(request)) {
            throw new RequestValidationException(
                    "Your registration request is not valid. " +
                            "Please check all fields and try again.");
        }

        if (optionalRole.isEmpty()) {
            log.error("Failed to find role");
            throw new SystemConfigurationException("System configuration error: USER role not found");
        }

        Result<Success> passwordValidation = validatePassword(
                request.password(), request.email(), request.name());
        if (passwordValidation.isError()) {
            String errorMessages = passwordValidation.getErrors().stream()
                    .map(Error::message)
                    .collect(java.util.stream.Collectors.joining("; "));
            log.warn("Invalid password during registration for email: {}. Errors: {}",
                    request.email(), errorMessages);
            throw new RequestValidationException(
                    "Password validation failed: " + errorMessages);
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                optionalRole.get());

        User savedUser = userRepository.save(user);
        if (verified) {
            savedUser.setIsVerified(true);
        }
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
        UUID userId = emailToUserId(email);
        return updateUser(userId, request);
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
        UUID userId = emailToUserId(email);
        deleteUser(userId);
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

        Result<Success> passwordValidation = validatePassword(newPassword, email, user.getName());
        if (passwordValidation.isError()) {
            String errorMessages = passwordValidation.getErrors().stream()
                    .map(Error::message)
                    .collect(java.util.stream.Collectors.joining("; "));
            log.warn("Invalid new password for user: {}. Errors: {}", email, errorMessages);
            throw new RequestValidationException(
                    "New password validation failed: " + errorMessages);
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