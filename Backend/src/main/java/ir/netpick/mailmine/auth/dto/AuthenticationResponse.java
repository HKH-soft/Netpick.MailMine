package ir.netpick.mailmine.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthenticationResponse(
                @JsonProperty("access_token") String accessToken,

                @JsonProperty("refresh_token") String refreshToken,

                @JsonProperty("expires_in") long expiresIn,

                @JsonProperty("token_type") String tokenType) {
        public AuthenticationResponse(String accessToken, String refreshToken, long expiresIn) {
                this(accessToken, refreshToken, expiresIn, "Bearer");
        }
}