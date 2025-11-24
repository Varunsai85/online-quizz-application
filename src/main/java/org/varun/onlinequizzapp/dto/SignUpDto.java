package org.varun.onlinequizzapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignUpDto(
        @NotNull(message = "Username cannot be null")
        String username,
        @NotNull(message = "Email cannot be null")
        @Email
        String email,
        @Size(min = 3, message = "Password must be at least 3 characters long")
        String password
) {
}
