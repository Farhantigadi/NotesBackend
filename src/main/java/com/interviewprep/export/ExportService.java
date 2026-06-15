package com.interviewprep.export;

import com.interviewprep.common.exception.ResourceNotFoundException;
import com.interviewprep.question.entity.Question;
import com.interviewprep.question.repository.QuestionRepository;
import com.interviewprep.section.entity.MainSection;
import com.interviewprep.section.repository.SectionRepository;
import com.interviewprep.subsection.entity.SubSection;
import com.interviewprep.subsection.repository.SubSectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final SectionRepository sectionRepository;
    private final SubSectionRepository subSectionRepository;
    private final QuestionRepository questionRepository;
    private final PdfGeneratorService pdfGeneratorService;

    public ExportService(SectionRepository sectionRepository,
                         SubSectionRepository subSectionRepository,
                         QuestionRepository questionRepository,
                         PdfGeneratorService pdfGeneratorService) {
        this.sectionRepository = sectionRepository;
        this.subSectionRepository = subSectionRepository;
        this.questionRepository = questionRepository;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    public byte[] exportSectionAsPdf(Long sectionId) {
        long start = System.currentTimeMillis();
        log.info("Export PDF requested for mainSectionId={}", sectionId);

        MainSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("MainSection", sectionId));

        log.info("Exporting section: '{}'", section.getTitle());

        List<SubSection> subSections = subSectionRepository.findByMainSectionIdOrderByDisplayOrderAsc(sectionId);
        List<Question> questions = questionRepository.findAllByMainSectionId(sectionId);

        log.info("Found {} subsections and {} questions for section '{}'",
                subSections.size(), questions.size(), section.getTitle());

        // Group questions by subSectionId for O(1) lookup
        Map<Long, List<Question>> questionsBySubSection = questions.stream()
                .collect(Collectors.groupingBy(q -> q.getSubSection().getId()));

        List<SectionExportDto.SubSectionExportDto> subDtos = subSections.stream()
                .map(sub -> SectionExportDto.SubSectionExportDto.builder()
                        .id(sub.getId())
                        .title(sub.getTitle())
                        .description(sub.getDescription())
                        .questions(toQuestionDtos(questionsBySubSection.getOrDefault(sub.getId(), List.of())))
                        .build())
                .toList();

        SectionExportDto exportDto = SectionExportDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .description(section.getDescription())
                .subSections(subDtos)
                .build();

        byte[] pdf = pdfGeneratorService.generate(exportDto);

        log.info("Export completed for '{}' — PDF size: {} bytes, time: {}ms",
                section.getTitle(), pdf.length, System.currentTimeMillis() - start);

        return pdf;
    }

    private List<SectionExportDto.QuestionExportDto> toQuestionDtos(List<Question> questions) {
        return questions.stream()
                .map(q -> SectionExportDto.QuestionExportDto.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .answer(q.getAnswer())
                        .codeSnippet(q.getCodeSnippet())
                        .codeLanguage(q.getCodeLanguage())
                        .explanation(q.getExplanation())
                        .displayOrder(q.getDisplayOrder())
                        .imageUrl(q.getImageUrl())
                        .imageWidth(q.getImageWidth())
                        .imageAlign(q.getImageAlign())
                        .build())
                .toList();
    }
}
