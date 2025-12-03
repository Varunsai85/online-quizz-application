package org.varun.onlinequizzapp.dto.quiz;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.varun.onlinequizzapp.model.type.Difficulty;

public record AddQuizDto(
        @NotNull(message = "Title of the quiz is required")
        @Size(min = 2,max = 100, message = "Name of the quiz must be between 2 and 100 characters")
        String title,
        @NotNull(message = "Description of the quiz is required")
        @Size(max = 500, message = "Description should be below 500 characters")
        String description,
        @NotNull(message = "Topic for the quiz is required")
        Long topicId,
        @NotNull(message = "Mention the time limit for he quiz")
        Integer timeLimit,
        @NotNull(message = "Select the difficult level of the quiz")
        Difficulty difficulty
) {
}
