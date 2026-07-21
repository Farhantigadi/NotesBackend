package com.interviewprep.export;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.jsoup.Jsoup;
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
import java.util.Map;

@Component
public class PdfGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(PdfGeneratorService.class);

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final BaseColor PAGE_BG         = new BaseColor(0xF5, 0xF0, 0xE8); // #F5F0E8
    private static final BaseColor TEXT_PRIMARY    = new BaseColor(0x24, 0x24, 0x24); // #242424
    private static final BaseColor TEXT_MUTED      = new BaseColor(0x6B, 0x6B, 0x6B); // #6b6b6b
    private static final BaseColor LABEL_COLOR     = new BaseColor(0xA8, 0xA2, 0x9E); // #a8a29e
    private static final BaseColor SEPARATOR_COLOR = new BaseColor(0xE8, 0xDF, 0xD0); // #e8dfd0
    private static final BaseColor CODE_BG         = new BaseColor(0x1E, 0x1E, 0x2E); // #1e1e2e
    private static final BaseColor CODE_HEADER_BG  = new BaseColor(0x18, 0x18, 0x25); // #181825
    private static final BaseColor CODE_BORDER     = new BaseColor(0x31, 0x32, 0x44); // #313244
    private static final BaseColor CODE_TEXT       = new BaseColor(0xCD, 0xD6, 0xF4); // #cdd6f4
    private static final BaseColor CODE_LANG       = new BaseColor(0x6C, 0x70, 0x86); // #6c7086

    // ── Sizes ─────────────────────────────────────────────────────────────────
    private static final float SIZE_SECTION    = 27f;
    private static final float SIZE_SUBSECTION = 21f;
    private static final float SIZE_QUESTION   = 16f;
    private static final float SIZE_BODY       = 13f;
    private static final float SIZE_LABEL      = 9f;
    private static final float SIZE_CODE       = 10f;
    private static final float SIZE_CODE_LANG  = 9f;
    private static final float SIZE_PAGE_NUM   = 9f;
    private static final float LEADING_BODY    = SIZE_BODY * 1.8f;

    // ── Margins ───────────────────────────────────────────────────────────────
    private static final float MARGIN_LEFT   = 60f;
    private static final float MARGIN_RIGHT  = 60f;
    private static final float MARGIN_TOP    = 50f;
    private static final float MARGIN_BOTTOM = 50f;

    // ── HTTP & JSON ───────────────────────────────────────────────────────────
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private final Font fontSectionTitle;
    private final Font fontSubsection;
    private final Font fontQuestion;
    private final Font fontBody;
    private final Font fontBodyMuted;
    private final Font fontLabel;
    private final Font fontPageNum;

    public PdfGeneratorService() {
        FontFactory.registerDirectories();

        BaseFont serifBold = loadBaseFont("Georgia Bold", BaseFont.TIMES_BOLD);
        BaseFont serifBase = loadBaseFont("Georgia",      BaseFont.TIMES_ROMAN);

        fontSectionTitle = new Font(serifBold, SIZE_SECTION,    Font.BOLD,   TEXT_PRIMARY);
        fontSubsection   = new Font(serifBold, SIZE_SUBSECTION, Font.BOLD,   TEXT_PRIMARY);
        fontQuestion     = new Font(serifBold, SIZE_QUESTION,   Font.BOLD,   TEXT_PRIMARY);
        fontBody         = new Font(serifBase, SIZE_BODY,       Font.NORMAL, TEXT_PRIMARY);
        fontBodyMuted    = new Font(serifBase, SIZE_BODY,       Font.NORMAL, TEXT_MUTED);
        fontLabel        = new Font(serifBold, SIZE_LABEL,      Font.BOLD,   LABEL_COLOR);
        fontPageNum      = new Font(serifBase, SIZE_PAGE_NUM,   Font.NORMAL, LABEL_COLOR);
    }

    // ── Public entry point ────────────────────────────────────────────────────

    public byte[] generate(SectionExportDto section) {
        log.info("PDF generation started for section: {}", section.getTitle());
        long start = System.currentTimeMillis();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPdfVersion(PdfWriter.VERSION_1_7);
            writer.setPageEvent(new PageDecorationEvent(PAGE_BG, fontPageNum));

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

    // ── Section header ────────────────────────────────────────────────────────

    private void addSectionHeader(Document doc, SectionExportDto section) throws DocumentException {
        Paragraph title = new Paragraph(section.getTitle(), fontSectionTitle);
        title.setSpacingBefore(8);
        title.setSpacingAfter(12);
        doc.add(title);

        addSeparator(doc, SEPARATOR_COLOR, 0.5f, 0, 16);

        if (hasText(section.getDescription())) {
            Paragraph desc = new Paragraph(section.getDescription(), fontBodyMuted);
            desc.setLeading(LEADING_BODY);
            desc.setSpacingAfter(24);
            doc.add(desc);
        } else {
            doc.add(spacer(16));
        }
    }

    // ── Subsection ────────────────────────────────────────────────────────────

    private void addSubSection(Document doc, SectionExportDto.SubSectionExportDto sub) throws DocumentException {
        doc.add(spacer(28));

        Paragraph subTitle = new Paragraph(sub.getTitle(), fontSubsection);
        subTitle.setSpacingAfter(10);
        doc.add(subTitle);

        addSeparator(doc, SEPARATOR_COLOR, 0.5f, 0, 12);

        if (hasText(sub.getDescription())) {
            Paragraph desc = new Paragraph(sub.getDescription(), fontBodyMuted);
            desc.setLeading(LEADING_BODY);
            desc.setSpacingAfter(14);
            doc.add(desc);
        }

        List<SectionExportDto.QuestionExportDto> questions = sub.getQuestions();
        if (questions == null || questions.isEmpty()) {
            Paragraph none = new Paragraph("No questions in this subsection.", fontBodyMuted);
            none.setSpacingAfter(10);
            doc.add(none);
        } else {
            for (int i = 0; i < questions.size(); i++) {
                addQuestion(doc, questions.get(i), i + 1);
            }
        }
    }

    // ── Question ──────────────────────────────────────────────────────────────

    private void addQuestion(Document doc, SectionExportDto.QuestionExportDto q, int index) throws DocumentException {
        doc.add(spacer(22));

        Paragraph qTitle = new Paragraph(index + ".  " + q.getTitle(), fontQuestion);
        qTitle.setSpacingAfter(10);
        doc.add(qTitle);

        if (hasText(q.getAnswer())) {
            addBodyBlock(doc, "ANSWER", q.getAnswer());
        }

        if (hasText(q.getImageUrl())) {
            addImage(doc, q.getImageUrl(), q.getImageWidth(), q.getImageAlign());
        }

        if (hasText(q.getCodeBlocks())) {
            try {
                List<Map<String, String>> blocks = OBJECT_MAPPER.readValue(
                        q.getCodeBlocks(), new TypeReference<>() {});
                for (Map<String, String> block : blocks) {
                    String code = block.get("code");
                    String language = block.get("language");
                    if (hasText(code)) {
                        addCodeBlock(doc, code, language);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse codeBlocks JSON for question id={}: {}", q.getId(), e.getMessage());
            }
        } else if (hasText(q.getCodeSnippet())) {
            addCodeBlock(doc, q.getCodeSnippet(), q.getCodeLanguage());
        }

        if (hasText(q.getExplanation())) {
            addBodyBlock(doc, "EXPLANATION", q.getExplanation());
        }
    }

    // ── Body block ────────────────────────────────────────────────────────────

    private void addBodyBlock(Document doc, String label, String content) throws DocumentException {
        Paragraph labelPara = new Paragraph(label, fontLabel);
        labelPara.setSpacingAfter(5);
        doc.add(labelPara);

        Paragraph body = new Paragraph(stripHtml(content), fontBody);
        body.setLeading(LEADING_BODY);
        body.setSpacingAfter(14);
        doc.add(body);
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return Jsoup.parse(html).text();
    }

    // ── Code block ────────────────────────────────────────────────────────────

    private void addCodeBlock(Document doc, String code, String language) throws DocumentException {
        doc.add(spacer(6));

        // Header bar (language label)
        if (hasText(language)) {
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingBefore(0);
            headerTable.setSpacingAfter(0);

            PdfPCell headerCell = new PdfPCell();
            headerCell.setCellEvent(new SolidBackgroundEvent(CODE_HEADER_BG));
            headerCell.setPaddingTop(7f);
            headerCell.setPaddingBottom(7f);
            headerCell.setPaddingLeft(16f);
            headerCell.setPaddingRight(16f);
            headerCell.setBorderWidthTop(1f);
            headerCell.setBorderWidthLeft(1f);
            headerCell.setBorderWidthRight(1f);
            headerCell.setBorderWidthBottom(0f);
            headerCell.setBorderColor(CODE_BORDER);

            Font langFont = new Font(Font.FontFamily.COURIER, SIZE_CODE_LANG, Font.BOLD, CODE_LANG);
            headerCell.addElement(new Paragraph(language.toUpperCase(), langFont));
            headerTable.addCell(headerCell);
            doc.add(headerTable);
        }

        // Code body
        PdfPTable codeTable = new PdfPTable(1);
        codeTable.setWidthPercentage(100);
        codeTable.setSpacingBefore(0);
        codeTable.setSpacingAfter(16);

        PdfPCell codeCell = new PdfPCell();
        codeCell.setCellEvent(new SolidBackgroundEvent(CODE_BG));
        codeCell.setPaddingTop(12f);
        codeCell.setPaddingBottom(12f);
        codeCell.setPaddingLeft(16f);
        codeCell.setPaddingRight(16f);
        codeCell.setBorderColor(CODE_BORDER);
        codeCell.setBorderWidth(1f);
        if (hasText(language)) {
            codeCell.setBorderWidthTop(0f);
        }
        codeCell.setUseBorderPadding(true);

        Font codeFont = new Font(Font.FontFamily.COURIER, SIZE_CODE, Font.NORMAL, CODE_TEXT);
        Paragraph codePara = new Paragraph(code, codeFont);
        codePara.setLeading(16f);
        codeCell.addElement(codePara);

        codeTable.addCell(codeCell);
        doc.add(codeTable);
    }

    // ── Image block ───────────────────────────────────────────────────────────

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

            float usableWidth = PageSize.A4.getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
            float widthPercent = (imageWidth != null && imageWidth > 0 && imageWidth <= 100)
                    ? imageWidth / 100f : 1f;
            float targetWidth = usableWidth * widthPercent;

            float scale = targetWidth / img.getWidth();
            img.scaleAbsolute(targetWidth, img.getHeight() * scale);
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

    // ── Helpers ───────────────────────────────────────────────────────────────

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

    // ── Font loader ───────────────────────────────────────────────────────────

    private BaseFont loadBaseFont(String name, String fallback) {
        String[] attempts = {
            "fonts/" + name.replace(" ", "") + ".ttf",
            "fonts/" + name.replace(" ", "-") + ".ttf"
        };
        for (String path : attempts) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
                }
            } catch (Exception ignored) {}
        }
        try {
            Font f = FontFactory.getFont(name, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
            if (f.getBaseFont() != null) return f.getBaseFont();
        } catch (Exception ignored) {}
        try {
            return BaseFont.createFont(fallback, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to load fallback font: " + fallback, e);
        }
    }

    // ── Cell background (paints over page bg) ─────────────────────────────────

    private static class SolidBackgroundEvent implements PdfPCellEvent {
        private final BaseColor color;

        SolidBackgroundEvent(BaseColor color) { this.color = color; }

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

    // ── Page decoration (background + page numbers) ───────────────────────────

    private static class PageDecorationEvent extends PdfPageEventHelper {
        private final BaseColor bg;
        private final Font pageNumFont;

        PageDecorationEvent(BaseColor bg, Font pageNumFont) {
            this.bg = bg;
            this.pageNumFont = pageNumFont;
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContentUnder();
            Rectangle page = document.getPageSize();
            canvas.setColorFill(bg);
            canvas.rectangle(page.getLeft(), page.getBottom(), page.getWidth(), page.getHeight());
            canvas.fill();
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            Rectangle page = document.getPageSize();
            String pageNum = String.valueOf(writer.getPageNumber());

            canvas.beginText();
            canvas.setFontAndSize(pageNumFont.getBaseFont(), SIZE_PAGE_NUM);
            canvas.setColorFill(pageNumFont.getColor());
            float x = (page.getLeft() + page.getRight()) / 2f;
            float y = page.getBottom() + 22f;
            canvas.showTextAligned(PdfContentByte.ALIGN_CENTER, pageNum, x, y, 0);
            canvas.endText();
        }
    }
}
