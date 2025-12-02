package org.varun.onlinequizzapp.dto;

import org.varun.onlinequizzapp.model.type.Role;

public record UserResponseDto(
        Long id,
        String username,
        String email,
        Role role,
        boolean isEnabled
) {
}
