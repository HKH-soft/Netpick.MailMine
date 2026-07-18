package ir.netpick.platform.gatekeeper.dto;

public record AuthenticationSigninRequest(
        String email,
        String password,
        String totpCode) {

    public AuthenticationSigninRequest(String email, String password) {
        this(email, password, null);
    }
}









