package org.varun.onlinequizzapp.dto.userAnswers;

import java.time.LocalDateTime;

public record UserAnswersResponseDto(
        Long id,
        Boolean isCorrect,
        LocalDateTime answeredAt
) {
}
