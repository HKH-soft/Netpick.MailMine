package ir.netpick.mailmine.common.exception;

import ir.netpick.mailmine.auth.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class DefaultExceptionHandler {
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiError> handleException(ResourceNotFoundException e,
                        HttpServletRequest request) {
                log.debug("Resource not found: {} - URI: {}", e.getMessage(), request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(InsufficientAuthenticationException.class)
        public ResponseEntity<ApiError> handleException(InsufficientAuthenticationException e,
                        HttpServletRequest request) {
                log.warn("Insufficient authentication attempt - URI: {}", request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.UNAUTHORIZED.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiError> handleException(BadCredentialsException e,
                        HttpServletRequest request) {
                log.warn("Bad credentials attempt - URI: {}", request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.UNAUTHORIZED.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiError> handleException(DuplicateResourceException e,
                        HttpServletRequest request) {
                log.debug("Duplicate resource: {} - URI: {}", e.getMessage(), request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.CONFLICT.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleException(AccessDeniedException e,
                        HttpServletRequest request) {
                log.warn("Access denied - URI: {} - User attempted unauthorized access", request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.FORBIDDEN.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ApiError> handleException(UsernameNotFoundException e,
                        HttpServletRequest request) {
                log.debug("Username not found - URI: {}", request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(AccountNotVerifiedException.class)
        public ResponseEntity<ApiError> handleException(AccountNotVerifiedException e,
                        HttpServletRequest request) {
                log.debug("Account not verified - URI: {}", request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.FORBIDDEN.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(RateLimitExceededException.class)
        public ResponseEntity<ApiError> handleException(RateLimitExceededException e,
                        HttpServletRequest request) {
                log.warn("Rate limit exceeded - URI: {} - Message: {}", request.getRequestURI(), e.getMessage());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.TOO_MANY_REQUESTS.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.TOO_MANY_REQUESTS);
        }

        @ExceptionHandler(UserAlreadyVerifiedException.class)
        public ResponseEntity<ApiError> handleException(UserAlreadyVerifiedException e,
                        HttpServletRequest request) {
                log.debug("User already verified - URI: {}", request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<ApiError> handleException(InvalidTokenException e,
                        HttpServletRequest request) {
                log.warn("Invalid token - URI: {}", request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.UNAUTHORIZED.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ApiError> handleException(UserNotFoundException e,
                        HttpServletRequest request) {
                log.debug("User not found - URI: {}", request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(VerificationCodeNotFoundException.class)
        public ResponseEntity<ApiError> handleException(VerificationCodeNotFoundException e,
                        HttpServletRequest request) {
                log.debug("Verification code not found - URI: {}", request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(VerificationException.class)
        public ResponseEntity<ApiError> handleException(VerificationException e,
                        HttpServletRequest request) {
                log.warn("Verification exception - URI: {} - Message: {}", request.getRequestURI(), e.getMessage());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(RequestValidationException.class)
        public ResponseEntity<ApiError> handleException(RequestValidationException e,
                        HttpServletRequest request) {
                log.debug("Request validation failed - URI: {} - Message: {}", request.getRequestURI(), e.getMessage());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiError> handleException(HttpMessageNotReadableException e,
                        HttpServletRequest request) {
                log.debug("Invalid request body - URI: {}", request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                "Invalid request body",
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleException(MethodArgumentNotValidException e,
                        HttpServletRequest request) {
                log.debug("Invalid request parameters - URI: {}", request.getRequestURI());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                "Invalid request parameters",
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiError> handleException(MethodArgumentTypeMismatchException e,
                        HttpServletRequest request) {
                log.debug("Invalid parameter format - URI: {} - Parameter: {}", request.getRequestURI(), e.getName());
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                "Invalid parameter format: " + e.getName(),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleException(Exception e,
                        HttpServletRequest request) {
                log.error("Unexpected error - URI: {} - Exception: {}", request.getRequestURI(), e.getClass().getSimpleName(), e);
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                "An unexpected error occurred",
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}