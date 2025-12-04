package org.varun.onlinequizzapp.dto.question;

import java.util.List;

public record QuestionResponseDto(
        Long id,
        String title,
        Long quizId,
        Integer order,
        List<OptionResponseDto> options
) {
}
