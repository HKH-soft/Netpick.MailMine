package ir.netpick.mailmine.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class VerificationException extends RuntimeException {
    public VerificationException(String message) {
        super(message);
    }
}
