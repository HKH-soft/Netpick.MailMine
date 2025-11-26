package ir.netpick.mailmine.auth;

public class AuthConstants {
    public static final String VERIFICATION_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String VERIFICATION_DIGITS = "0123456789";
    public static final int VERIFICATION_CODE_LETTER_COUNT = 4;
    public static final int VERIFICATION_CODE_DIGIT_COUNT = 4;
    public static final int VERIFICATION_ACCOUNT_EXPIRATION_TIME_HOUR = 10;
    public static final int VERIFICATION_CODE_EXPIRATION_TIME_MIN = 10;
    public static final int VERIFICATION_MAX_ATTEMPTS = 5;
}
