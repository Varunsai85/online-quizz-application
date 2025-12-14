package org.varun.onlinequizzapp.dto.quizAttempt;

import jakarta.validation.constraints.NotNull;

public record SubmitAnswerDto(
        @NotNull(message = "Question ID is required")
        Long questionId,
        @NotNull(message = "Selected option ID is required")
        Long selectedOptionId
) {
}
