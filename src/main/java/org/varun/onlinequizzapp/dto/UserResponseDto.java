package org.varun.onlinequizzapp.dto;

import org.varun.onlinequizzapp.model.type.Role;

public record UserResponseDto(
        String username,
        String email,
        Role role
) {
}
