package org.varun.onlinequizzapp.dto.question;

import org.varun.onlinequizzapp.dto.option.OptionResponseDto;
import java.util.List;

public record QuestionResponseDto(
        Long id,
        String title,
        Long quizId,
        List<OptionResponseDto> options
) {
}
