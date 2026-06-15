package com.interviewprep.export;

import com.interviewprep.section.repository.SectionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sections")
@Tag(name = "Export", description = "PDF export APIs")
public class ExportController {

    private static final Logger log = LoggerFactory.getLogger(ExportController.class);

    private final ExportService exportService;
    private final SectionRepository sectionRepository;

    public ExportController(ExportService exportService, SectionRepository sectionRepository) {
        this.exportService = exportService;
        this.sectionRepository = sectionRepository;
    }

    @GetMapping("/{id}/export-pdf")
    @Operation(summary = "Export a main section and all its content as a PDF")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        log.info("GET /api/sections/{}/export-pdf received", id);

        String filename = sectionRepository.findById(id)
                .map(s -> sanitizeFilename(s.getTitle()) + ".pdf")
                .orElse("section-" + id + ".pdf");

        byte[] pdf = exportService.exportSectionAsPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(pdf.length);

        log.info("Returning PDF '{}' ({} bytes)", filename, pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
