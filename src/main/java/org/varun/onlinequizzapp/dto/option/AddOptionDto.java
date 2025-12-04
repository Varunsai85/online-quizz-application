package org.varun.onlinequizzapp.dto.option;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddOptionDto(
        @NotNull(message = "Option cannot be empty")
        @Size(min = 1,max = 100,message = "Option must be between 1 to 100 characters long")
        String optionText,
        @NotNull(message = "Specify if this option is correct")
        Boolean isCorrect
) {
}
