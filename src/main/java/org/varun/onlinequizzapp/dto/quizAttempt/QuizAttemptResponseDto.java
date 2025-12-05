package org.varun.onlinequizzapp.dto.quizAttempt;

import org.varun.onlinequizzapp.dto.userAnswers.UserAnswersResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public record QuizAttemptResponseDto(
        Long id,
        Integer score,
        Integer totalQuestions,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        Boolean isCompleted,
        List<UserAnswersResponseDto> userAnswers
) {
}
