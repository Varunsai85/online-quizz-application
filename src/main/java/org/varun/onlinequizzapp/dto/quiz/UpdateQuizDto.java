package org.varun.onlinequizzapp.dto.quiz;

import jakarta.validation.constraints.Size;
import org.varun.onlinequizzapp.model.type.Difficulty;

public record UpdateQuizDto(
        @Size(min = 2,max = 100, message = "Name of the quiz must be between 2 and 100 characters")
        String title,
        @Size(max = 500, message = "Description should be below 500 characters")
        String description,
        Long topicId,
        Integer timeLimit,
        Difficulty difficulty
) {
}
