package org.varun.onlinequizzapp.dto.topic;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddTopicDto(
        @NotNull(message = "Name of the topic is required")
        String name,
        @NotNull(message = "Description for the topic is required")
        @Size(max = 500,message = "Description cannot exceed 500 characters")
        String description
) {
}
