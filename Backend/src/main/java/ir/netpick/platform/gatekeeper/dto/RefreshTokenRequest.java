package ir.netpick.platform.gatekeeper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required") @JsonProperty("refresh_token") String refreshToken) {
}









