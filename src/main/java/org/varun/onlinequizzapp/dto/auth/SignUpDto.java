package org.varun.onlinequizzapp.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignUpDto(
        @NotNull(message = "Username is required")
        String username,
        @NotNull(message = "Email is required")
        @Email
        String email,
        @NotNull(message = "Password is required")
        @Size(min = 3, message = "Password must be at least 3 characters long")
        String password
) {
}
