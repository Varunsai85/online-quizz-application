package org.varun.onlinequizzapp.dto.question;

public record OptionResponseDto(
        Long id,
        String optionText,
        Boolean isCorrect
) {
}
