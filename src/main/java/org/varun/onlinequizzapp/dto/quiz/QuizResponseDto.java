package org.varun.onlinequizzapp.dto.quiz;

import org.varun.onlinequizzapp.model.type.Difficulty;

import java.time.LocalDateTime;

public record QuizResponseDto(
        Long id,
        String title,
        String description,
        String topicName,
        Integer timeLimit,
        Difficulty difficulty,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
