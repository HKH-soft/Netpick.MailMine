package ir.netpick.platform.gatekeeper.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class VerificationCodeNotFoundException extends RuntimeException {
    public VerificationCodeNotFoundException(String message) {
        super(message);
    }
    
    public VerificationCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}








