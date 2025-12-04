package org.varun.onlinequizzapp.dto.option;

public record OptionResponseDto(
        Long id,
        String optionText,
        Boolean isCorrect
) {
}
