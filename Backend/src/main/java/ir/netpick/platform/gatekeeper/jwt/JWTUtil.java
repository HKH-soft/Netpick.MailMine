package ir.netpick.platform.gatekeeper.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.MINUTES;

@Slf4j
@Service
public class JWTUtil {

    private final JWTKeyRotationService keyRotationService;

    @Value("${security.jwt.access-expiration-minutes:15}")
    private long accessTokenExpirationMinutes;

    @Value("${security.jwt.issuer}")
    private String issuer;

    public JWTUtil(JWTKeyRotationService keyRotationService) {
        this.keyRotationService = keyRotationService;
    }

    public String issueToken(String subject) {
        return issueToken(subject, Map.of());
    }

    public String issueToken(String subject, String... scopes) {
        return issueToken(subject, Map.of("scopes", scopes));
    }

    public String issueToken(String subject, List<String> scopes) {
        return issueToken(subject, Map.of("scopes", scopes));
    }

    public String issueToken(
            String subject,
            Map<String, Object> claims) {

        return Jwts
                .builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(accessTokenExpirationMinutes, MINUTES)))
                .header().keyId(keyRotationService.getKeyId())
                .and()
                .signWith(keyRotationService.getSigningKey())
                .compact();
    }

    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Get the expiration time in minutes.
     * 
     * @return expiration time in minutes
     */
    public long getAccessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    private Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    private Claims getClaims(String token) {
        // Try current signing key first
        Key currentKey = keyRotationService.getSigningKey();
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) currentKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            // If current key fails, try old keys from Redis
            String keyId = Jwts.parser()
                    .verifyWith((SecretKey) currentKey)
                    .build()
                    .parseSignedClaims(token)
                    .getHeader()
                    .getKeyId();
            
            Key oldKey = keyRotationService.getVerificationKey(keyId);
            if (oldKey != null) {
                return Jwts.parser()
                        .verifyWith((SecretKey) oldKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            }
            throw e;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getSubject(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

}









