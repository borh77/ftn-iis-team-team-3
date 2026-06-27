package com.example.iisdrugcrm.service.order;

import com.example.iisdrugcrm.dto.order.OrderDocumentItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderDocumentParserTest {

    @Test
    void jsonParserParsesValidDocument() throws Exception {
        JsonOrderDocumentParser parser = new JsonOrderDocumentParser(new ObjectMapper());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "order.json",
                "application/json",
                "[{\"variantId\":10,\"requestedQuantity\":150}]".getBytes()
        );

        List<OrderDocumentItemDTO> items = parser.parse(file);

        assertEquals(1, items.size());
        assertEquals(10L, items.get(0).getVariantId());
        assertEquals(150, items.get(0).getRequestedQuantity());
    }

    @Test
    void csvParserParsesValidDocument() throws Exception {
        CsvOrderDocumentParser parser = new CsvOrderDocumentParser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "order.csv",
                "text/csv",
                "variantId,requestedQuantity\n10,150\n11,50\n".getBytes()
        );

        List<OrderDocumentItemDTO> items = parser.parse(file);

        assertEquals(2, items.size());
        assertEquals(10L, items.get(0).getVariantId());
        assertEquals(150, items.get(0).getRequestedQuantity());
        assertEquals(11L, items.get(1).getVariantId());
        assertEquals(50, items.get(1).getRequestedQuantity());
    }

    @Test
    void resolverRejectsUnsupportedFormat() {
        OrderDocumentParserResolver resolver = new OrderDocumentParserResolver(List.of(
                new JsonOrderDocumentParser(new ObjectMapper()),
                new CsvOrderDocumentParser()
        ));
        MockMultipartFile file = new MockMultipartFile("file", "order.txt", "text/plain", "demo".getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> resolver.resolve(file));

        assertEquals("Unsupported order document format. Please upload a JSON or CSV file.", exception.getMessage());
    }
}
