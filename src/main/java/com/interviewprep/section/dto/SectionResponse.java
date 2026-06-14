package com.interviewprep.section.dto;

import java.time.LocalDateTime;

public record SectionResponse(
        Long id,
        String title,
        String description,
        Integer displayOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
