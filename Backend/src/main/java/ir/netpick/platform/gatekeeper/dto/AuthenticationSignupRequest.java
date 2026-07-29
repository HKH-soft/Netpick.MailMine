package ir.netpick.platform.gatekeeper.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationSignupRequest(
                @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
                @NotBlank(message = "Password is required") @Size(min = 12, message = "Password must be at least 12 characters") String password,
                @NotBlank(message = "Name is required") String name) {

}









