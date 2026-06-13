package com.interviewprep.section.dto;

import java.time.LocalDateTime;

public record SectionResponse(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
