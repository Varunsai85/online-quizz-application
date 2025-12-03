package org.varun.onlinequizzapp.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ResendDto(
        @Email(message = "Enter Valid Email ID")
        @NotNull(message = "Email cannot be null")
        String email
) {
}
