package ir.netpick.platform.mailmine.config;

import ch.qos.logback.core.pattern.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Logback converter to mask sensitive data in logs.
 * Masks passwords, secrets, tokens, API keys, and authorization headers.
 */
@Slf4j
public class SensitiveDataConverter extends Converter<Object> {

    private static final Pattern[] SENSITIVE_PATTERNS = {
        // Password patterns
        Pattern.compile("(?i)password[\"']?\\s*[:=]\\s*[\"']?[a-zA-Z0-9!@#$%^&*()_+\\-=]+[\"']?", 
            Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)passwd[\"']?\\s*[:=]\\s*[\"']?[a-zA-Z0-9!@#$%^&*()_+\\-=]+[\"']?", 
            Pattern.CASE_INSENSITIVE),
        
        // Secret patterns
        Pattern.compile("(?i)secret[\"']?\\s*[:=]\\s*[\"']?[a-zA-Z0-9]+[\"']?", 
            Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)secretKey[\"']?\\s*[:=]\\s*[\"']?[a-zA-Z0-9]+[\"']?", 
            Pattern.CASE_INSENSITIVE),
        
        // Token patterns
        Pattern.compile("(?i)token[\"']?\\s*[:=]\\s*[\"']?[a-zA-Z0-9\\-_\\.]+[\"']?", 
            Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)accessToken[\"']?\\s*[:=]\\s*[\"']?[a-zA-Z0-9\\-_\\.]+[\"']?", 
            Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)refreshToken[\"']?\\s*[:=]\\s*[\"']?[a-zA-Z0-9\\-_\\.]+[\"']?", 
            Pattern.CASE_INSENSITIVE),
        
        // API key patterns
        Pattern.compile("(?i)apiKey[\"']?\\s*[:=]\\s*[\"']?[a-zA-Z0-9]+[\"']?", 
            Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)api-key[\"']?\\s*[:=]\\s*[\"']?[a-zA-Z0-9]+[\"']?", 
            Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)api_key[\"']?\\s*[:=]\\s*[\"']?[a-zA-Z0-9]+[\"']?", 
            Pattern.CASE_INSENSITIVE),
        
        // Authorization header
        Pattern.compile("(?i)authorization[\"']?\\s*[:=]\\s*[\"']?Bearer\\s+[a-zA-Z0-9\\-_\\.]+[\"']?", 
            Pattern.CASE_INSENSITIVE),
        
        // JWT tokens in URLs
        Pattern.compile("(?i)jwt=([a-zA-Z0-9\\-_\\.]+)", 
            Pattern.CASE_INSENSITIVE),
    };

    @Override
    public String convert(Object obj) {
        if (obj == null) {
            return "";
        }
        String message = obj.toString();
        return maskSensitiveData(message);
    }

    /**
     * Mask sensitive data in the message
     */
    public static String maskSensitiveData(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String masked = message;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            masked = pattern.matcher(masked)
                .replaceAll(match -> {
                    String fullMatch = match.group();
                    if (fullMatch.contains(":")) {
                        String[] parts = fullMatch.split("[:=]", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            return key + "=***MASKED***";
                        }
                    } else if (fullMatch.contains("Bearer ")) {
                        String[] parts = fullMatch.split("Bearer\\s+", 2);
                        if (parts.length == 2) {
                            return parts[0] + "Bearer ***MASKED***";
                        }
                    }
                    return "***MASKED***";
                });
        }
        return masked;
    }
}