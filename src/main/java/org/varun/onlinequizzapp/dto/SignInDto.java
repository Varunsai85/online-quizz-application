package org.varun.onlinequizzapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignInDto(
        @NotNull(message = "Username cannot be null")
        String login,
        @Size(min = 3, message = "Password must be at least 3 characters long")
        String password
) {
}
