package com.example.iisdrugcrm.service.order;

import com.example.iisdrugcrm.dto.order.OrderDocumentItemDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class JsonOrderDocumentParser implements OrderDocumentParser {

    private final ObjectMapper objectMapper;

    public JsonOrderDocumentParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        return hasExtension(filename, ".json") || "application/json".equalsIgnoreCase(contentType);
    }

    @Override
    public List<OrderDocumentItemDTO> parse(MultipartFile file) throws IOException {
        return objectMapper.readValue(file.getInputStream(), new TypeReference<>() {
        });
    }

    private boolean hasExtension(String filename, String extension) {
        return filename != null && filename.toLowerCase().endsWith(extension);
    }
}
