package com.interviewprep.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuestionCreateRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        String answer,
        String codeSnippet,

        @Size(max = 100, message = "Code language must not exceed 100 characters")
        String codeLanguage,

        String explanation,
        Integer displayOrder,
        Integer imageWidth,

        @Size(max = 10, message = "Image align must be left, center or right")
        String imageAlign,

        @NotNull(message = "Sub-section ID must not be null")
        Long subSectionId
) {}
