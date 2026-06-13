package com.interviewprep.subsection.dto;

import java.time.LocalDateTime;

public record SubSectionResponse(
        Long id,
        String title,
        String description,
        Long mainSectionId,
        String mainSectionTitle,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
