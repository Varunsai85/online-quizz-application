package org.varun.onlinequizzapp.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record VerificationCodeDto(
        @NotNull(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotNull(message = "Code is required")
        String code
) {
}
