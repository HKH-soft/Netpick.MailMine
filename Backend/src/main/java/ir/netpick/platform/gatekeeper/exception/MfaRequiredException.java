package ir.netpick.platform.gatekeeper.exception;

public class MfaRequiredException extends RuntimeException {

    private final String email;

    public MfaRequiredException(String message, String email) {
        super(message);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
