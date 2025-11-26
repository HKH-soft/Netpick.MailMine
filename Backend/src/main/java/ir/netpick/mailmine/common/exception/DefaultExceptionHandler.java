package ir.netpick.mailmine.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@ControllerAdvice
public class DefaultExceptionHandler {
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiError> handleException(ResourceNotFoundException e,
                        HttpServletRequest request) {
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
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.FORBIDDEN.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiError> handleException(BadCredentialsException e,
                        HttpServletRequest request) {
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
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleException(Exception e,
                        HttpServletRequest request) {
                ApiError apiError = new ApiError(
                                request.getRequestURI(),
                                e.getMessage(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                LocalDateTime.now());
                return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
