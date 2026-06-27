package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.MonthlyPerformancePointDTO;
import com.example.iisdrugcrm.dto.pricelist.TeamPerformanceReportDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PerformanceReportPdfService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final Color HEADER_BACKGROUND = new Color(230, 236, 245);

    private final PricelistActivityLogService activityLogService;

    public PerformanceReportPdfService(PricelistActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    public byte[] generatePerformanceReportPdf(Long teamId, OffsetDateTime start, OffsetDateTime end) {
        TeamPerformanceReportDTO report = activityLogService.getPerformanceReport(teamId, start, end);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 42, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addTitle(document);
            addMetadata(document, report);
            addKpis(document, report);
            addMonthlyTrend(document, report.getMonthlyTrend());
            addLimitationNote(document, report.getTeamFilterLimitation());

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException exception) {
            throw new IllegalStateException("Unable to generate performance report PDF.", exception);
        }
    }

    private void addTitle(Document document) throws DocumentException {
        Paragraph title = new Paragraph("Izveštaj o performansama timova", font(18, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(18);
        document.add(title);
    }

    private void addMetadata(Document document, TeamPerformanceReportDTO report) throws DocumentException {
        PdfPTable table = table(2);
        table.addCell(labelCell("Tim"));
        table.addCell(valueCell(report.getTeamId() == null ? "Svi timovi" : "Team ID " + report.getTeamId()));
        table.addCell(labelCell("Period od"));
        table.addCell(valueCell(formatTimestamp(report.getPeriodStart())));
        table.addCell(labelCell("Period do"));
        table.addCell(valueCell(formatTimestamp(report.getPeriodEnd())));
        table.addCell(labelCell("Generisano"));
        table.addCell(valueCell(formatTimestamp(OffsetDateTime.now(ZoneOffset.UTC))));
        table.setSpacingAfter(16);
        document.add(table);
    }

    private void addKpis(Document document, TeamPerformanceReportDTO report) throws DocumentException {
        Paragraph heading = new Paragraph("KPI", font(14, Font.BOLD));
        heading.setSpacingAfter(8);
        document.add(heading);

        PdfPTable table = table(2);
        table.addCell(labelCell("Prosečno vreme obrade"));
        table.addCell(valueCell(formatHours(report.getAverageTotalProcessingTimeHours())));
        table.addCell(labelCell("Prosečno vreme u review fazi"));
        table.addCell(valueCell(formatHours(report.getAverageReviewTimeHours())));
        table.addCell(labelCell("Broj aktiviranih cenovnika"));
        table.addCell(valueCell(String.valueOf(nullSafe(report.getActivatedPricelistsCount()))));
        table.addCell(labelCell("Broj zaglavljenih u DRAFT"));
        table.addCell(valueCell(String.valueOf(nullSafe(report.getStuckDraftCount()))));
        table.addCell(labelCell("Broj zaglavljenih u IN_REVIEW"));
        table.addCell(valueCell(String.valueOf(nullSafe(report.getStuckInReviewCount()))));
        table.setSpacingAfter(16);
        document.add(table);
    }

    private void addMonthlyTrend(Document document, List<MonthlyPerformancePointDTO> monthlyTrend) throws DocumentException {
        Paragraph heading = new Paragraph("Mesečni trend", font(14, Font.BOLD));
        heading.setSpacingAfter(8);
        document.add(heading);

        PdfPTable table = table(3);
        table.addCell(headerCell("Mesec"));
        table.addCell(headerCell("Prosečno vreme obrade (h)"));
        table.addCell(headerCell("Aktivirani cenovnici"));

        if (monthlyTrend == null || monthlyTrend.isEmpty()) {
            PdfPCell emptyCell = valueCell("Nema podataka za izabrani period.");
            emptyCell.setColspan(3);
            table.addCell(emptyCell);
        } else {
            for (MonthlyPerformancePointDTO point : monthlyTrend) {
                table.addCell(valueCell(point.getMonth()));
                table.addCell(valueCell(formatDecimal(point.getAverageTotalProcessingTimeHours())));
                table.addCell(valueCell(String.valueOf(nullSafe(point.getActivatedPricelistsCount()))));
            }
        }

        table.setSpacingAfter(12);
        document.add(table);
    }

    private void addLimitationNote(Document document, String limitation) throws DocumentException {
        if (limitation == null || limitation.isBlank()) {
            return;
        }

        Paragraph note = new Paragraph("Napomena: " + limitation, font(9, Font.ITALIC));
        note.setSpacingBefore(8);
        document.add(note);
    }

    private PdfPTable table(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        return table;
    }

    private PdfPCell labelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(10, Font.BOLD)));
        cell.setBackgroundColor(HEADER_BACKGROUND);
        cell.setPadding(7);
        return cell;
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(10, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(HEADER_BACKGROUND);
        cell.setPadding(7);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font(10, Font.NORMAL)));
        cell.setPadding(7);
        return cell;
    }

    private Font font(int size, int style) {
        return FontFactory.getFont(FontFactory.HELVETICA, "Cp1250", true, size, style, Color.BLACK);
    }

    private String formatTimestamp(OffsetDateTime timestamp) {
        return timestamp == null ? "" : TIMESTAMP_FORMATTER.format(timestamp.withOffsetSameInstant(ZoneOffset.UTC));
    }

    private String formatHours(BigDecimal hours) {
        return formatDecimal(hours) + " h";
    }

    private String formatDecimal(BigDecimal value) {
        return nullSafe(value).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long nullSafe(Long value) {
        return value == null ? 0L : value;
    }
}
