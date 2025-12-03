package org.varun.onlinequizzapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignInDto(
        @NotNull(message = "Username or Email is required")
        String login,
        @NotNull(message = "Password is required")
        @Size(min = 3, message = "Password must be at least 3 characters long")
        String password
) {
}
