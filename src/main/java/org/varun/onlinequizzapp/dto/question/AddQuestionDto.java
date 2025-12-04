package org.varun.onlinequizzapp.dto.question;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.varun.onlinequizzapp.dto.option.AddOptionDto;

import java.util.List;

public record AddQuestionDto(
        @NotNull(message = "Title is required for the question")
        @Size(min = 3, max = 500, message = "Title for the question must be between 3 to 500 characters")
        String title,
        @NotNull(message = "Id for thr quiz is required")
        Long quizId,
        @NotNull(message = "Order for the question is required")
        Integer order,
        @NotNull(message = "Options for the question is required")
        @Size(min = 2,message = "At least 2 options are required")
        @Valid
        List<AddOptionDto> options
) {
}
