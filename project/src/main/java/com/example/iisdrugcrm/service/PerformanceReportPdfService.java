package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistTeam;
import com.example.iisdrugcrm.dto.pricelist.MonthlyPerformancePointDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistDashboardSummaryDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PerformanceReportPdfService {

    private static final Color HEADER_BACKGROUND = new Color(230, 236, 245);

    private final PricelistActivityLogService activityLogService;
    private final PricelistDashboardService dashboardService;
    private final PdfReportDisplayFormatter displayFormatter;

    public PerformanceReportPdfService(
            PricelistActivityLogService activityLogService,
            PricelistDashboardService dashboardService,
            PdfReportDisplayFormatter displayFormatter
    ) {
        this.activityLogService = activityLogService;
        this.dashboardService = dashboardService;
        this.displayFormatter = displayFormatter;
    }

    public byte[] generatePerformanceReportPdf(Long teamId, OffsetDateTime start, OffsetDateTime end) {
        TeamPerformanceReportDTO report = activityLogService.getPerformanceReport(teamId, start, end);
        PricelistDashboardSummaryDTO dashboard = dashboardService.getSummary(null, true, start, end, teamId, null, null, null);
        Map<Long, PricelistTeam> teamsById = displayFormatter.teamsById(teamIds(report, dashboard));

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 42, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addTitle(document);
            addMetadata(document, report, teamsById);
            addDashboardSummary(document, dashboard, teamsById);
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
        Paragraph title = new Paragraph("Team Performance Report", font(18, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(18);
        document.add(title);
    }

    private void addMetadata(
            Document document,
            TeamPerformanceReportDTO report,
            Map<Long, PricelistTeam> teamsById
    ) throws DocumentException {
        PdfPTable table = table(2);
        table.addCell(labelCell("Team"));
        table.addCell(valueCell(displayFormatter.formatTeamFilter(report.getTeamId(), teamsById)));
        table.addCell(labelCell("Period start"));
        table.addCell(valueCell(displayFormatter.formatDateTime(report.getPeriodStart())));
        table.addCell(labelCell("Period end"));
        table.addCell(valueCell(displayFormatter.formatDateTime(report.getPeriodEnd())));
        table.addCell(labelCell("Generated"));
        table.addCell(valueCell(displayFormatter.formatDateTime(OffsetDateTime.now())));
        table.setSpacingAfter(16);
        document.add(table);
    }

    private void addDashboardSummary(
            Document document,
            PricelistDashboardSummaryDTO summary,
            Map<Long, PricelistTeam> teamsById
    ) throws DocumentException {
        Paragraph heading = new Paragraph("Pricelist Dashboard Summary", font(14, Font.BOLD));
        heading.setSpacingAfter(8);
        document.add(heading);

        PdfPTable kpiTable = table(4);
        kpiTable.addCell(labelCell("Total pricelists"));
        kpiTable.addCell(valueCell(String.valueOf(summary.getTotalPricelists())));
        kpiTable.addCell(labelCell("Active price lists"));
        kpiTable.addCell(valueCell(String.valueOf(summary.getActiveCount())));
        kpiTable.addCell(labelCell("Avg. approval time"));
        kpiTable.addCell(valueCell(formatDays(summary.getAverageReviewTimeHours())));
        kpiTable.addCell(labelCell("Active offers"));
        kpiTable.addCell(valueCell(String.valueOf(summary.getActiveOffersCount())));
        kpiTable.addCell(labelCell("Draft"));
        kpiTable.addCell(valueCell(String.valueOf(summary.getDraftCount())));
        kpiTable.addCell(labelCell("In review"));
        kpiTable.addCell(valueCell(String.valueOf(summary.getInReviewCount())));
        kpiTable.addCell(labelCell("Archived"));
        kpiTable.addCell(valueCell(String.valueOf(summary.getArchivedCount())));
        kpiTable.addCell(labelCell("Activated in period"));
        kpiTable.addCell(valueCell(String.valueOf(summary.getActivatedPricelistsCount())));
        kpiTable.setSpacingAfter(12);
        document.add(kpiTable);

        addConversionFunnel(document, summary);
        addTeamBreakdownTable(document, summary.getPricelistsByTeam(), teamsById);
        addBreakdownTable(document, "Price lists by region", summary.getPricelistsByRegion());
    }

    private void addConversionFunnel(Document document, PricelistDashboardSummaryDTO summary) throws DocumentException {
        Paragraph heading = new Paragraph("Price List Conversion Funnel", font(11, Font.BOLD));
        heading.setSpacingAfter(6);
        document.add(heading);

        PdfPTable table = table(2);
        table.addCell(headerCell("Step"));
        table.addCell(headerCell("Count"));
        table.addCell(valueCell("Price lists created"));
        table.addCell(valueCell(String.valueOf(summary.getTotalPricelists())));
        table.addCell(valueCell("Submitted for review"));
        table.addCell(valueCell(String.valueOf(summary.getInReviewCount() + summary.getActiveCount() + summary.getArchivedCount())));
        table.addCell(valueCell("Activated"));
        table.addCell(valueCell(String.valueOf(summary.getActivatedPricelistsCount())));
        table.setSpacingAfter(12);
        document.add(table);
    }

    private void addBreakdownTable(
            Document document,
            String title,
            List<PricelistDashboardSummaryDTO.BreakdownItemDTO> rows
    ) throws DocumentException {
        Paragraph heading = new Paragraph(title, font(11, Font.BOLD));
        heading.setSpacingAfter(6);
        document.add(heading);

        PdfPTable table = table(2);
        table.addCell(headerCell("Label"));
        table.addCell(headerCell("Count"));
        if (rows == null || rows.isEmpty()) {
            PdfPCell emptyCell = valueCell("No data for the selected filters.");
            emptyCell.setColspan(2);
            table.addCell(emptyCell);
        } else {
            for (PricelistDashboardSummaryDTO.BreakdownItemDTO row : rows) {
                table.addCell(valueCell(row.getLabel()));
                table.addCell(valueCell(String.valueOf(row.getCount())));
            }
        }
        table.setSpacingAfter(12);
        document.add(table);
    }

    private void addTeamBreakdownTable(
            Document document,
            List<PricelistDashboardSummaryDTO.BreakdownItemDTO> rows,
            Map<Long, PricelistTeam> teamsById
    ) throws DocumentException {
        Paragraph heading = new Paragraph("Team Productivity", font(11, Font.BOLD));
        heading.setSpacingAfter(6);
        document.add(heading);

        PdfPTable table = table(2);
        table.addCell(headerCell("Label"));
        table.addCell(headerCell("Count"));
        if (rows == null || rows.isEmpty()) {
            PdfPCell emptyCell = valueCell("No data for the selected filters.");
            emptyCell.setColspan(2);
            table.addCell(emptyCell);
        } else {
            for (PricelistDashboardSummaryDTO.BreakdownItemDTO row : rows) {
                table.addCell(valueCell(row.getId() == null ? row.getLabel() : displayFormatter.formatTeam(row.getId(), teamsById)));
                table.addCell(valueCell(String.valueOf(row.getCount())));
            }
        }
        table.setSpacingAfter(12);
        document.add(table);
    }

    private void addKpis(Document document, TeamPerformanceReportDTO report) throws DocumentException {
        Paragraph heading = new Paragraph("KPI", font(14, Font.BOLD));
        heading.setSpacingAfter(8);
        document.add(heading);

        PdfPTable table = table(2);
        table.addCell(labelCell("Average processing time"));
        table.addCell(valueCell(formatHours(report.getAverageTotalProcessingTimeHours())));
        table.addCell(labelCell("Average review time"));
        table.addCell(valueCell(formatHours(report.getAverageReviewTimeHours())));
        table.addCell(labelCell("Activated pricelists"));
        table.addCell(valueCell(String.valueOf(nullSafe(report.getActivatedPricelistsCount()))));
        table.addCell(labelCell("Pricelists stuck in DRAFT"));
        table.addCell(valueCell(String.valueOf(nullSafe(report.getStuckDraftCount()))));
        table.addCell(labelCell("Pricelists stuck in IN_REVIEW"));
        table.addCell(valueCell(String.valueOf(nullSafe(report.getStuckInReviewCount()))));
        table.setSpacingAfter(16);
        document.add(table);
    }

    private void addMonthlyTrend(Document document, List<MonthlyPerformancePointDTO> monthlyTrend) throws DocumentException {
        Paragraph heading = new Paragraph("Monthly trend", font(14, Font.BOLD));
        heading.setSpacingAfter(8);
        document.add(heading);

        PdfPTable table = table(3);
        table.addCell(headerCell("Month"));
        table.addCell(headerCell("Average processing time (h)"));
        table.addCell(headerCell("Activated pricelists"));

        if (monthlyTrend == null || monthlyTrend.isEmpty()) {
            PdfPCell emptyCell = valueCell("No data for the selected period.");
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

        Paragraph note = new Paragraph("Note: " + limitation, font(9, Font.ITALIC));
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

    private String formatHours(BigDecimal hours) {
        return formatDecimal(hours) + " h";
    }

    private String formatDays(BigDecimal hours) {
        return nullSafe(hours).divide(BigDecimal.valueOf(24), 1, java.math.RoundingMode.HALF_UP).toPlainString() + " days";
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

    private List<Long> teamIds(TeamPerformanceReportDTO report, PricelistDashboardSummaryDTO dashboard) {
        List<Long> ids = new ArrayList<>();
        if (report.getTeamId() != null) {
            ids.add(report.getTeamId());
        }
        if (dashboard.getPricelistsByTeam() != null) {
            dashboard.getPricelistsByTeam().stream()
                    .map(PricelistDashboardSummaryDTO.BreakdownItemDTO::getId)
                    .filter(id -> id != null)
                    .forEach(ids::add);
        }
        return ids;
    }
}
