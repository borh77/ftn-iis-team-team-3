package com.example.iisdrugcrm.service.order;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class OrderDocumentParserResolver {

    private static final String CSV_ONLY_MESSAGE = "Only CSV procurement documents are currently supported.";

    private final CsvOrderDocumentParser csvParser;

    public OrderDocumentParserResolver(List<OrderDocumentParser> parsers) {
        this.csvParser = parsers.stream()
                .filter(CsvOrderDocumentParser.class::isInstance)
                .map(CsvOrderDocumentParser.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("CSV order document parser is not configured"));
    }

    public OrderDocumentParser resolve(MultipartFile file) {
        if (!csvParser.supports(file)) {
            throw new IllegalArgumentException(CSV_ONLY_MESSAGE);
        }
        return csvParser;
    }
}
