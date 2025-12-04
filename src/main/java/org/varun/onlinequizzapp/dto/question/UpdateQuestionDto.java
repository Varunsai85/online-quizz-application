package org.varun.onlinequizzapp.dto.question;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateQuestionDto(
        @NotNull(message = "Title of the question is required")
        @Size(min = 3, max = 500, message = "Title for the question must be between 3 to 500 characters")
        String title,
        @NotNull(message = "Order for the question is required")
        @Positive
        Integer order
) {
}
