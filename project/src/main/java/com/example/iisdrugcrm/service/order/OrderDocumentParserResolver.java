package com.example.iisdrugcrm.service.order;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class OrderDocumentParserResolver {

    private final List<OrderDocumentParser> parsers;

    public OrderDocumentParserResolver(List<OrderDocumentParser> parsers) {
        this.parsers = parsers;
    }

    public OrderDocumentParser resolve(MultipartFile file) {
        return parsers.stream()
                .filter(parser -> parser.supports(file))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported order document format. Please upload a JSON or CSV file."));
    }
}
