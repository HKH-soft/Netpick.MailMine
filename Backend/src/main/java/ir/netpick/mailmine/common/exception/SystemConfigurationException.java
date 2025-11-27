package ir.netpick.mailmine.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is a system configuration error.
 * This typically indicates a problem with the application setup or missing required data.
 */
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class SystemConfigurationException extends RuntimeException {
    /**
     * Constructs a new SystemConfigurationException with the specified detail message.
     *
     * @param message the detail message
     */
    public SystemConfigurationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new SystemConfigurationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public SystemConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}