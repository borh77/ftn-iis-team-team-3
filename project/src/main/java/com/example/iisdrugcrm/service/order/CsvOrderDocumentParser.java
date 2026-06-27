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

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] columns = line.split(",", -1);
                if (columns.length != 2) {
                    throw new IllegalArgumentException("Invalid CSV row at line " + lineNumber + ". Expected variantId,requestedQuantity.");
                }
                try {
                    Long variantId = Long.valueOf(columns[0].trim());
                    Integer requestedQuantity = Integer.valueOf(columns[1].trim());
                    items.add(new OrderDocumentItemDTO(variantId, requestedQuantity));
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("Invalid number in CSV row at line " + lineNumber + ".");
                }
            }
        }
        return items;
    }

    private void validateHeader(String header) {
        String normalized = header.trim().replace(" ", "").toLowerCase();
        if (!"variantid,requestedquantity".equals(normalized)) {
            throw new IllegalArgumentException("CSV header must be variantId,requestedQuantity");
        }
    }

    private boolean hasExtension(String filename, String extension) {
        return filename != null && filename.toLowerCase().endsWith(extension);
    }
}
