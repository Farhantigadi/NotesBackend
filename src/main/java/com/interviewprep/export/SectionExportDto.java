package com.interviewprep.export;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SectionExportDto {
    private final Long id;
    private final String title;
    private final String description;
    private final List<SubSectionExportDto> subSections;

    @Getter
    @Builder
    public static class SubSectionExportDto {
        private final Long id;
        private final String title;
        private final String description;
        private final List<QuestionExportDto> questions;
    }

    @Getter
    @Builder
    public static class QuestionExportDto {
        private final Long id;
        private final String title;
        private final String answer;
        private final String codeSnippet;
        private final String codeLanguage;
        private final String explanation;
        private final Integer displayOrder;
        private final String imageUrl;
        private final Integer imageWidth;
        private final String imageAlign;
    }
}
