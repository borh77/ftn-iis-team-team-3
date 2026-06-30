package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.MarketLicenseResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseStatusCountDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketProductCountByRegionDTO;
import com.example.iisdrugcrm.dto.portfolio.ProductCountByTherapeuticAreaDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionStatusCountDTO;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PortfolioReportPdfService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final Color HEADER_BACKGROUND =
            new Color(230, 236, 245);

    private final PortfolioAnalyticsService portfolioAnalyticsService;
    private final MarketLicenseService marketLicenseService;

    public PortfolioReportPdfService(
            PortfolioAnalyticsService portfolioAnalyticsService,
            MarketLicenseService marketLicenseService
    ) {
        this.portfolioAnalyticsService = portfolioAnalyticsService;
        this.marketLicenseService = marketLicenseService;
    }

    public byte[] generateAnalyticsReportPdf(LocalDate expiringUntil) {
        LocalDate effectiveExpiringUntil = expiringUntil == null
                ? LocalDate.now().plusMonths(6)
                : expiringUntil;

        List<VariantVersionStatusCountDTO> versionStatusCounts =
                portfolioAnalyticsService.getVariantVersionStatusCount();

        List<ProductCountByTherapeuticAreaDTO> productsByTherapeuticArea =
                portfolioAnalyticsService.getActiveProductCountByTherapeuticArea();

        List<MarketLicenseStatusCountDTO> licenseStatusCounts =
                portfolioAnalyticsService.getMarketLicenseStatusCount();

        List<MarketProductCountByRegionDTO> marketProductsByRegion =
                portfolioAnalyticsService.getActiveMarketProductCountByRegion();

        List<MarketLicenseResponseDTO> expiringLicenses =
                marketLicenseService.getLicensesExpiringUntil(effectiveExpiringUntil);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 42, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addTitle(document);
            addMetadata(document, effectiveExpiringUntil);
            addSummary(
                    document,
                    versionStatusCounts,
                    productsByTherapeuticArea,
                    licenseStatusCounts,
                    marketProductsByRegion,
                    expiringLicenses
            );
            addVersionStatusCounts(document, versionStatusCounts);
            addLicenseStatusCounts(document, licenseStatusCounts);
            addProductsByTherapeuticArea(document, productsByTherapeuticArea);
            addMarketProductsByRegion(document, marketProductsByRegion);
            addExpiringLicenses(document, expiringLicenses, effectiveExpiringUntil);

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException exception) {
            throw new IllegalStateException("Unable to generate portfolio analytics report PDF.", exception);
        }
    }

    private void addTitle(Document document) throws DocumentException {
        Paragraph title = new Paragraph("Portfolio Analytics Report", font(18, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(18);
        document.add(title);
    }

    private void addMetadata(Document document, LocalDate expiringUntil) throws DocumentException {
        PdfPTable table = table(2);

        table.addCell(labelCell("Report"));
        table.addCell(valueCell("Product Portfolio Analytics"));

        table.addCell(labelCell("Generated on"));
        table.addCell(valueCell(formatTimestamp(OffsetDateTime.now(ZoneOffset.UTC))));

        table.addCell(labelCell("Licenses expiring until"));
        table.addCell(valueCell(expiringUntil.toString()));

        table.setSpacingAfter(16);
        document.add(table);
    }

    private void addSummary(
            Document document,
            List<VariantVersionStatusCountDTO> versionStatusCounts,
            List<ProductCountByTherapeuticAreaDTO> productsByTherapeuticArea,
            List<MarketLicenseStatusCountDTO> licenseStatusCounts,
            List<MarketProductCountByRegionDTO> marketProductsByRegion,
            List<MarketLicenseResponseDTO> expiringLicenses
    ) throws DocumentException {
        Paragraph heading = heading("Summary");
        document.add(heading);

        PdfPTable table = table(2);

        table.addCell(labelCell("Total variant versions"));
        table.addCell(valueCell(String.valueOf(sumVariantVersions(versionStatusCounts))));

        table.addCell(labelCell("Active products in analytics"));
        table.addCell(valueCell(String.valueOf(sumProducts(productsByTherapeuticArea))));

        table.addCell(labelCell("Total market licenses"));
        table.addCell(valueCell(String.valueOf(sumMarketLicenses(licenseStatusCounts))));

        table.addCell(labelCell("Active market products"));
        table.addCell(valueCell(String.valueOf(sumMarketProducts(marketProductsByRegion))));

        table.addCell(labelCell("Expiring licenses"));
        table.addCell(valueCell(String.valueOf(expiringLicenses == null ? 0 : expiringLicenses.size())));

        table.setSpacingAfter(16);
        document.add(table);
    }

    private void addVersionStatusCounts(
            Document document,
            List<VariantVersionStatusCountDTO> items
    ) throws DocumentException {
        document.add(heading("Variant versions by status"));

        PdfPTable table = table(2);
        table.addCell(headerCell("Status"));
        table.addCell(headerCell("Count"));

        if (items == null || items.isEmpty()) {
            emptyRow(table, 2, "No variant version status data.");
        } else {
            for (VariantVersionStatusCountDTO item : items) {
                table.addCell(valueCell(item.getStatus().name()));
                table.addCell(valueCell(String.valueOf(item.getCount())));
            }
        }

        table.setSpacingAfter(16);
        document.add(table);
    }

    private void addLicenseStatusCounts(
            Document document,
            List<MarketLicenseStatusCountDTO> items
    ) throws DocumentException {
        document.add(heading("Market licenses by status"));

        PdfPTable table = table(2);
        table.addCell(headerCell("Status"));
        table.addCell(headerCell("Count"));

        if (items == null || items.isEmpty()) {
            emptyRow(table, 2, "No market license status data.");
        } else {
            for (MarketLicenseStatusCountDTO item : items) {
                table.addCell(valueCell(item.getStatus().name()));
                table.addCell(valueCell(String.valueOf(item.getCount())));
            }
        }

        table.setSpacingAfter(16);
        document.add(table);
    }

    private void addProductsByTherapeuticArea(
            Document document,
            List<ProductCountByTherapeuticAreaDTO> items
    ) throws DocumentException {
        document.add(heading("Active products by therapeutic area"));

        PdfPTable table = table(2);
        table.addCell(headerCell("Therapeutic area"));
        table.addCell(headerCell("Product count"));

        if (items == null || items.isEmpty()) {
            emptyRow(table, 2, "No therapeutic area data.");
        } else {
            for (ProductCountByTherapeuticAreaDTO item : items) {
                table.addCell(valueCell(item.getTherapeuticAreaName()));
                table.addCell(valueCell(String.valueOf(item.getProductCount())));
            }
        }

        table.setSpacingAfter(16);
        document.add(table);
    }

    private void addMarketProductsByRegion(
            Document document,
            List<MarketProductCountByRegionDTO> items
    ) throws DocumentException {
        document.add(heading("Market products by region"));

        PdfPTable table = table(3);
        table.addCell(headerCell("Region"));
        table.addCell(headerCell("Code"));
        table.addCell(headerCell("Market products"));

        if (items == null || items.isEmpty()) {
            emptyRow(table, 3, "No market products by region data.");
        } else {
            for (MarketProductCountByRegionDTO item : items) {
                table.addCell(valueCell(item.getRegionName()));
                table.addCell(valueCell(item.getRegionCode()));
                table.addCell(valueCell(String.valueOf(item.getMarketProductCount())));
            }
        }

        table.setSpacingAfter(16);
        document.add(table);
    }

    private void addExpiringLicenses(
            Document document,
            List<MarketLicenseResponseDTO> items,
            LocalDate expiringUntil
    ) throws DocumentException {
        document.add(heading("Licenses expiring until " + expiringUntil));

        PdfPTable table = table(5);
        table.addCell(headerCell("License"));
        table.addCell(headerCell("Product"));
        table.addCell(headerCell("Region"));
        table.addCell(headerCell("Status"));
        table.addCell(headerCell("Valid until"));

        if (items == null || items.isEmpty()) {
            emptyRow(table, 5, "No expiring licenses.");
        } else {
            for (MarketLicenseResponseDTO item : items) {
                table.addCell(valueCell(item.getLicenseNumber()));
                table.addCell(valueCell(item.getProductName() + " / " + item.getVersionLabel()));
                table.addCell(valueCell(item.getRegionName() + " (" + item.getRegionCode() + ")"));
                table.addCell(valueCell(item.getStatus()));
                table.addCell(valueCell(item.getValidUntil() == null ? "-" : item.getValidUntil().toString()));
            }
        }

        table.setSpacingAfter(12);
        document.add(table);
    }

    private Paragraph heading(String text) {
        Paragraph heading = new Paragraph(text, font(14, Font.BOLD));
        heading.setSpacingAfter(8);
        return heading;
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

    private void emptyRow(PdfPTable table, int columns, String message) {
        PdfPCell emptyCell = valueCell(message);
        emptyCell.setColspan(columns);
        table.addCell(emptyCell);
    }

    private Font font(int size, int style) {
        return FontFactory.getFont(FontFactory.HELVETICA, "Cp1250", true, size, style, Color.BLACK);
    }

    private String formatTimestamp(OffsetDateTime timestamp) {
        if (timestamp == null) {
            return "";
        }

        return TIMESTAMP_FORMATTER.format(
                timestamp.withOffsetSameInstant(ZoneOffset.ofHours(2))
        ) + " UTC+2";
    }

    private long sumVariantVersions(List<VariantVersionStatusCountDTO> items) {
        return items == null ? 0 : items.stream().mapToLong(VariantVersionStatusCountDTO::getCount).sum();
    }

    private long sumProducts(List<ProductCountByTherapeuticAreaDTO> items) {
        return items == null ? 0 : items.stream().mapToLong(ProductCountByTherapeuticAreaDTO::getProductCount).sum();
    }

    private long sumMarketLicenses(List<MarketLicenseStatusCountDTO> items) {
        return items == null ? 0 : items.stream().mapToLong(MarketLicenseStatusCountDTO::getCount).sum();
    }

    private long sumMarketProducts(List<MarketProductCountByRegionDTO> items) {
        return items == null ? 0 : items.stream().mapToLong(MarketProductCountByRegionDTO::getMarketProductCount).sum();
    }
}