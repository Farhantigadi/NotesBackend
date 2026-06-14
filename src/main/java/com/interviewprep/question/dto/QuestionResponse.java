package com.interviewprep.question.dto;

import java.time.LocalDateTime;

public record QuestionResponse(
        Long id,
        String title,
        String answer,
        String codeSnippet,
        String codeLanguage,
        String explanation,
        Integer displayOrder,
        Long subSectionId,
        String subSectionTitle,
        String imageUrl,
        Integer imageWidth,
        String imageAlign,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
