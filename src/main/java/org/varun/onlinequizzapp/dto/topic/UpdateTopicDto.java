package org.varun.onlinequizzapp.dto.topic;

import jakarta.validation.constraints.Size;

public record UpdateTopicDto(
        String name,
        @Size(max = 500,message = "Description cannot exceed 500 characters")
        String description
) {
}
