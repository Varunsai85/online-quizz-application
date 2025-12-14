package org.varun.onlinequizzapp.dto.quizAttempt;

public record AnswerFeedbackDto(
        Long questionId,
        String questionTitle,
        Long selectedOptionId,
        String selectedOptionText,
        Boolean isCorrect,
        Long correctOptionId,
        String correctOptionText
) {
}
