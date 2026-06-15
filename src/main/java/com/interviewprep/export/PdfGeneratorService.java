package com.interviewprep.export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Component
public class PdfGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(PdfGeneratorService.class);

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final BaseColor PAGE_BG          = new BaseColor(0xF5, 0xF0, 0xE8); // #F5F0E8
    private static final BaseColor TEXT_PRIMARY     = new BaseColor(0x24, 0x24, 0x24); // #242424
    private static final BaseColor TEXT_SECONDARY   = new BaseColor(0x6B, 0x6B, 0x6B); // #6b6b6b
    private static final BaseColor CODE_BG          = new BaseColor(0x1E, 0x1E, 0x2E); // #1e1e2e
    private static final BaseColor CODE_HEADER_BG   = new BaseColor(0x18, 0x18, 0x25); // #181825
    private static final BaseColor CODE_BORDER      = new BaseColor(0x31, 0x32, 0x44); // #313244
    private static final BaseColor CODE_TEXT        = new BaseColor(0xCD, 0xD6, 0xF4); // catppuccin text
    private static final BaseColor CODE_LANG_TEXT   = new BaseColor(0x89, 0xB4, 0xFA); // catppuccin blue
    private static final BaseColor SEPARATOR_COLOR  = new BaseColor(0xD4, 0xC9, 0xB0); // warm separator

    // ── Font sizes (px → pt: multiply by 0.75) ────────────────────────────────
    // Section title  36px → 27pt
    // Subsection     42px → 31.5pt  (capped slightly for A4 layout)
    // Question       24px → 18pt
    // Body           20px → 15pt
    // Code           14px → 10.5pt
    private static final float SIZE_SECTION_TITLE   = 27f;
    private static final float SIZE_SUBSECTION      = 24f;
    private static final float SIZE_QUESTION        = 18f;
    private static final float SIZE_BODY            = 13f;
    private static final float SIZE_LABEL           = 10f;
    private static final float SIZE_CODE            = 10.5f;
    private static final float SIZE_CODE_LANG       = 9f;
    private static final float LEADING_BODY         = SIZE_BODY * 1.8f; // line-height 1.8

    // ── HTTP client for image downloading ─────────────────────────────────────
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // ── Fonts (registered at startup, fallback to built-ins) ──────────────────
    private final Font fontSectionTitle;
    private final Font fontSubsection;
    private final Font fontQuestion;
    private final Font fontBody;
    private final Font fontBodyMuted;
    private final Font fontLabel;
    private final Font fontCode;
    private final Font fontCodeLang;

    public PdfGeneratorService() {
        FontFactory.registerDirectories();

        // Serif font for titles — tries Georgia (Windows) / DejaVu Serif (Linux), falls back to Times
        BaseFont serifBase  = loadBaseFont("Georgia", BaseFont.TIMES_ROMAN);
        BaseFont serifBold  = loadBaseFont("Georgia Bold", BaseFont.TIMES_BOLD);

        // Serif for body (Source Serif Pro / Georgia fallback)
        BaseFont bodyBase   = loadBaseFont("Georgia", BaseFont.TIMES_ROMAN);

        // Monospace for code — tries Courier New, falls back to Courier
        BaseFont monoBase   = loadBaseFont("Courier New", BaseFont.COURIER);
        BaseFont monoBold   = loadBaseFont("Courier New Bold", BaseFont.COURIER_BOLD);

        fontSectionTitle = new Font(serifBold,  SIZE_SECTION_TITLE, Font.BOLD,   TEXT_PRIMARY);
        fontSubsection   = new Font(serifBold,  SIZE_SUBSECTION,    Font.BOLD,   TEXT_PRIMARY);
        fontQuestion     = new Font(serifBold,  SIZE_QUESTION,      Font.BOLD,   TEXT_PRIMARY);
        fontBody         = new Font(bodyBase,   SIZE_BODY,          Font.NORMAL, TEXT_PRIMARY);
        fontBodyMuted    = new Font(bodyBase,   SIZE_BODY,          Font.NORMAL, TEXT_SECONDARY);
        fontLabel        = new Font(serifBold,  SIZE_LABEL,         Font.BOLD,   TEXT_SECONDARY);
        fontCode         = new Font(monoBase,   SIZE_CODE,          Font.NORMAL, CODE_TEXT);
        fontCodeLang     = new Font(monoBold,   SIZE_CODE_LANG,     Font.BOLD,   CODE_LANG_TEXT);
    }

    // ── Public entry point ─────────────────────────────────────────────────────

    public byte[] generate(SectionExportDto section) {
        log.info("PDF generation started for section: {}", section.getTitle());
        long start = System.currentTimeMillis();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 56, 56, 64, 64);
            PdfWriter writer = PdfWriter.getInstance(doc, out);

            // Page background event
            writer.setPageEvent(new PageBackgroundEvent(PAGE_BG));

            doc.open();

            addSectionHeader(doc, section);

            List<SectionExportDto.SubSectionExportDto> subSections = section.getSubSections();
            if (subSections == null || subSections.isEmpty()) {
                doc.add(new Paragraph("No subsections available.", fontBodyMuted));
            } else {
                for (SectionExportDto.SubSectionExportDto sub : subSections) {
                    addSubSection(doc, sub);
                }
            }

            doc.close();

            long elapsed = System.currentTimeMillis() - start;
            log.info("PDF generation completed — size: {} bytes, time: {}ms", out.size(), elapsed);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("PDF generation failed for section id={}: {}", section.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    // ── Section header ─────────────────────────────────────────────────────────

    private void addSectionHeader(Document doc, SectionExportDto section) throws DocumentException {
        Paragraph title = new Paragraph(section.getTitle(), fontSectionTitle);
        title.setSpacingBefore(8);
        title.setSpacingAfter(10);
        doc.add(title);

        addSeparator(doc, SEPARATOR_COLOR, 1.5f, 0, 16);

        if (hasText(section.getDescription())) {
            Paragraph desc = new Paragraph(section.getDescription(), fontBodyMuted);
            desc.setLeading(LEADING_BODY);
            desc.setSpacingAfter(24);
            doc.add(desc);
        } else {
            doc.add(spacer(16));
        }
    }

    // ── Subsection ─────────────────────────────────────────────────────────────

    private void addSubSection(Document doc, SectionExportDto.SubSectionExportDto sub) throws DocumentException {
        doc.add(spacer(24));

        Paragraph subTitle = new Paragraph(sub.getTitle(), fontSubsection);
        subTitle.setSpacingAfter(8);
        doc.add(subTitle);

        addSeparator(doc, SEPARATOR_COLOR, 0.75f, 0, 12);

        if (hasText(sub.getDescription())) {
            Paragraph desc = new Paragraph(sub.getDescription(), fontBodyMuted);
            desc.setLeading(LEADING_BODY);
            desc.setSpacingAfter(16);
            doc.add(desc);
        }

        List<SectionExportDto.QuestionExportDto> questions = sub.getQuestions();
        if (questions == null || questions.isEmpty()) {
            Paragraph none = new Paragraph("No questions in this subsection.", fontBodyMuted);
            none.setIndentationLeft(12);
            none.setSpacingAfter(12);
            doc.add(none);
        } else {
            for (int i = 0; i < questions.size(); i++) {
                addQuestion(doc, questions.get(i), i + 1);
            }
        }
    }

    // ── Question ───────────────────────────────────────────────────────────────

    private void addQuestion(Document doc, SectionExportDto.QuestionExportDto q, int index) throws DocumentException {
        doc.add(spacer(20));

        Paragraph qTitle = new Paragraph(index + ".  " + q.getTitle(), fontQuestion);
        qTitle.setSpacingAfter(10);
        doc.add(qTitle);

        if (hasText(q.getAnswer())) {
            addBodyBlock(doc, "Answer", q.getAnswer());
        }

        if (hasText(q.getImageUrl())) {
            addImage(doc, q.getImageUrl(), q.getImageWidth(), q.getImageAlign());
        }

        if (hasText(q.getCodeSnippet())) {
            addCodeBlock(doc, q.getCodeSnippet(), q.getCodeLanguage());
        }

        if (hasText(q.getExplanation())) {
            addBodyBlock(doc, "Explanation", q.getExplanation());
        }
    }

    // ── Image block ───────────────────────────────────────────────────

    private void addImage(Document doc, String imageUrl, Integer imageWidth, String imageAlign) throws DocumentException {
        log.info("Image URL found: {}", imageUrl);

        byte[] imageBytes = downloadImage(imageUrl);
        if (imageBytes == null) {
            Paragraph placeholder = new Paragraph("[Image could not be loaded]", fontBodyMuted);
            placeholder.setSpacingBefore(8);
            placeholder.setSpacingAfter(8);
            doc.add(placeholder);
            return;
        }

        try {
            Image img = Image.getInstance(imageBytes);

            // usable width = A4 width minus left+right margins (56+56)
            float usableWidth = PageSize.A4.getWidth() - 112f;

            // apply imageWidth percentage (default 100%)
            float widthPercent = (imageWidth != null && imageWidth > 0 && imageWidth <= 100)
                    ? imageWidth / 100f : 1f;
            float targetWidth = usableWidth * widthPercent;

            // scale preserving aspect ratio
            float scale = targetWidth / img.getWidth();
            img.scaleAbsolute(targetWidth, img.getHeight() * scale);

            // alignment
            img.setAlignment(resolveAlignment(imageAlign));

            img.setSpacingBefore(10);
            img.setSpacingAfter(14);

            doc.add(img);
            log.info("Image embedded successfully from URL: {}", imageUrl);

        } catch (Exception e) {
            log.error("Unsupported image format or embed error for URL={}: {}", imageUrl, e.getMessage());
            Paragraph placeholder = new Paragraph("[Image could not be loaded]", fontBodyMuted);
            placeholder.setSpacingBefore(8);
            placeholder.setSpacingAfter(8);
            doc.add(placeholder);
        }
    }

    private byte[] downloadImage(String imageUrl) {
        log.info("Image download started: {}", imageUrl);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                log.warn("Image download failed — HTTP {}: {}", response.statusCode(), imageUrl);
                return null;
            }

            log.info("Image download successful: {} ({} bytes)", imageUrl, response.body().length);
            return response.body();

        } catch (Exception e) {
            log.error("Image download failed for URL={}: {}", imageUrl, e.getMessage());
            return null;
        }
    }

    private int resolveAlignment(String imageAlign) {
        if (imageAlign == null) return Image.ALIGN_CENTER;
        return switch (imageAlign.toLowerCase()) {
            case "left"  -> Image.ALIGN_LEFT;
            case "right" -> Image.ALIGN_RIGHT;
            default      -> Image.ALIGN_CENTER;
        };
    }

    // ── Body block (Answer / Explanation) ─────────────────────────────────────

    private void addBodyBlock(Document doc, String label, String content) throws DocumentException {
        Paragraph labelPara = new Paragraph(label, fontLabel);
        labelPara.setSpacingAfter(4);
        doc.add(labelPara);

        Paragraph body = new Paragraph(content, fontBody);
        body.setLeading(LEADING_BODY);
        body.setSpacingAfter(14);
        doc.add(body);
    }

    // ── Code block ─────────────────────────────────────────────────────────────

    private void addCodeBlock(Document doc, String code, String language) throws DocumentException {
        doc.add(spacer(4));

        PdfPTable codeTable = new PdfPTable(1);
        codeTable.setWidthPercentage(100);
        codeTable.setSpacingBefore(0);
        codeTable.setSpacingAfter(16);
        codeTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell codeCell = new PdfPCell();
        codeCell.setCellEvent(new SolidBackgroundEvent(new BaseColor(0x1E, 0x1E, 0x2E)));
        codeCell.setPadding(10f);
        codeCell.setBorderColor(new BaseColor(0x31, 0x32, 0x44));
        codeCell.setBorderWidth(1f);
        codeCell.setUseBorderPadding(true);

        if (hasText(language)) {
            Font labelFont = new Font(Font.FontFamily.COURIER, 9f, Font.BOLD,
                    new BaseColor(0x6C, 0x70, 0x86));
            Paragraph langLabel = new Paragraph(language.toUpperCase(), labelFont);
            langLabel.setSpacingAfter(6f);
            codeCell.addElement(langLabel);
        }

        Font codeFont = new Font(Font.FontFamily.COURIER, 10f, Font.NORMAL,
                new BaseColor(0xCD, 0xD6, 0xF4));
        Paragraph codePara = new Paragraph(code, codeFont);
        codePara.setLeading(16f);
        codeCell.addElement(codePara);

        codeTable.addCell(codeCell);
        doc.add(codeTable);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void addSeparator(Document doc, BaseColor color, float lineWidth,
                              float spacingBefore, float spacingAfter) throws DocumentException {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        line.setSpacingBefore(spacingBefore);
        line.setSpacingAfter(spacingAfter);

        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorderWidthBottom(lineWidth);
        cell.setBorderColorBottom(color);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setPaddingBottom(2);
        line.addCell(cell);

        doc.add(line);
    }

    private Paragraph spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setLeading(height);
        return p;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // ── Font loader with fallback ──────────────────────────────────────────────

    private BaseFont loadBaseFont(String name, String fallback) {
        // Try classpath fonts first (drop .ttf files in src/main/resources/fonts/)
        String[] classpathAttempts = {
            "fonts/" + name.replace(" ", "") + ".ttf",
            "fonts/" + name.replace(" ", "-") + ".ttf"
        };
        for (String path : classpathAttempts) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                            true, bytes, null);
                }
            } catch (Exception ignored) {}
        }

        // Try system font by name via FontFactory
        try {
            Font f = FontFactory.getFont(name, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
            if (f.getBaseFont() != null) return f.getBaseFont();
        } catch (Exception ignored) {}

        // Fall back to built-in iText font
        try {
            return BaseFont.createFont(fallback, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to load fallback font: " + fallback, e);
        }
    }

    // ── Cell background event (paints over page bg) ───────────────────────────

    private static class SolidBackgroundEvent implements PdfPCellEvent {
        private final BaseColor color;

        SolidBackgroundEvent(BaseColor color) {
            this.color = color;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.BACKGROUNDCANVAS];
            canvas.saveState();
            canvas.setColorFill(color);
            canvas.rectangle(position.getLeft(), position.getBottom(),
                    position.getWidth(), position.getHeight());
            canvas.fill();
            canvas.restoreState();
        }
    }

    // ── Page background event ──────────────────────────────────────────────────

    private static class PageBackgroundEvent extends PdfPageEventHelper {
        private final BaseColor bg;

        PageBackgroundEvent(BaseColor bg) {
            this.bg = bg;
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContentUnder();
            Rectangle page = document.getPageSize();
            canvas.setColorFill(bg);
            canvas.rectangle(page.getLeft(), page.getBottom(), page.getWidth(), page.getHeight());
            canvas.fill();
        }
    }
}
