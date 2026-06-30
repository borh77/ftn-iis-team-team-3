package com.example.iisdrugcrm.service.order;

import com.example.iisdrugcrm.dto.order.OrderDocumentItemDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CsvOrderDocumentParser implements OrderDocumentParser {

    @Override
    public boolean supports(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        return hasExtension(filename, ".csv")
                || "text/csv".equalsIgnoreCase(contentType)
                || "application/csv".equalsIgnoreCase(contentType);
    }

    @Override
    public List<OrderDocumentItemDTO> parse(MultipartFile file) throws IOException {
        List<OrderDocumentItemDTO> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || header.isBlank()) {
                throw new IllegalArgumentException("CSV document is empty");
            }
            validateHeader(header);
            CsvFormat format = CsvFormat.fromHeader(header);

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] columns = line.split(",", -1);
                if (columns.length != format.columnCount()) {
                    throw new IllegalArgumentException("Invalid CSV row at line " + lineNumber + ". Expected " + format.expectedColumns() + ".");
                }
                try {
                    items.add(format.toItem(columns));
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("Invalid number in CSV row at line " + lineNumber + ".");
                }
            }
        }
        return items;
    }

    private void validateHeader(String header) {
        String normalized = header.trim().replace(" ", "").toLowerCase();
        if (!CsvFormat.supports(normalized)) {
            throw new IllegalArgumentException("CSV header must be variantId,requestedQuantity; variantName,requestedQuantity; or productName,form,dosage,requestedQuantity");
        }
    }

    private boolean hasExtension(String filename, String extension) {
        return filename != null && filename.toLowerCase().endsWith(extension);
    }

    private enum CsvFormat {
        VARIANT_ID("variantid,requestedquantity", 2, "variantId,requestedQuantity") {
            @Override
            OrderDocumentItemDTO toItem(String[] columns) {
                return new OrderDocumentItemDTO(Long.valueOf(columns[0].trim()), Integer.valueOf(columns[1].trim()));
            }
        },
        VARIANT_NAME("variantname,requestedquantity", 2, "variantName,requestedQuantity") {
            @Override
            OrderDocumentItemDTO toItem(String[] columns) {
                return new OrderDocumentItemDTO(columns[0].trim(), Integer.valueOf(columns[1].trim()));
            }
        },
        STRUCTURED("productname,form,dosage,requestedquantity", 4, "productName,form,dosage,requestedQuantity") {
            @Override
            OrderDocumentItemDTO toItem(String[] columns) {
                return new OrderDocumentItemDTO(
                        columns[0].trim(),
                        columns[1].trim(),
                        columns[2].trim(),
                        Integer.valueOf(columns[3].trim())
                );
            }
        };

        private final String normalizedHeader;
        private final int columnCount;
        private final String expectedColumns;

        CsvFormat(String normalizedHeader, int columnCount, String expectedColumns) {
            this.normalizedHeader = normalizedHeader;
            this.columnCount = columnCount;
            this.expectedColumns = expectedColumns;
        }

        abstract OrderDocumentItemDTO toItem(String[] columns);

        int columnCount() {
            return columnCount;
        }

        String expectedColumns() {
            return expectedColumns;
        }

        static boolean supports(String normalizedHeader) {
            for (CsvFormat format : values()) {
                if (format.normalizedHeader.equals(normalizedHeader)) {
                    return true;
                }
            }
            return false;
        }

        static CsvFormat fromHeader(String header) {
            String normalized = header.trim().replace(" ", "").toLowerCase();
            for (CsvFormat format : values()) {
                if (format.normalizedHeader.equals(normalized)) {
                    return format;
                }
            }
            throw new IllegalArgumentException("CSV header must be variantId,requestedQuantity; variantName,requestedQuantity; or productName,form,dosage,requestedQuantity");
        }
    }
}
