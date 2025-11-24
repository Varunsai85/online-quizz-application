package org.varun.onlinequizzapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SignUpRequest(
        @NotNull(message = "Username cannot be null")
        String username,
        @NotNull(message = "Email cannot be null")
        @Email
        String email,
        @Min(value = 3, message = "Password must be at least 6 characters long")
        String password
) {
}
