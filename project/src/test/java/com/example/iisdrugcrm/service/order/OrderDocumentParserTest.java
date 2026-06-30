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
    void csvParserParsesValidDocument() throws Exception {
        CsvOrderDocumentParser parser = new CsvOrderDocumentParser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "order.csv",
                "text/csv",
                "variantId,requestedQuantity\n2,10\n".getBytes()
        );

        List<OrderDocumentItemDTO> items = parser.parse(file);

        assertEquals(1, items.size());
        assertEquals(2L, items.get(0).getVariantId());
        assertEquals(10, items.get(0).getRequestedQuantity());
    }

    @Test
    void csvParserParsesVariantNameDocument() throws Exception {
        CsvOrderDocumentParser parser = new CsvOrderDocumentParser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "order.csv",
                "text/csv",
                "variantName,requestedQuantity\nBrufen LIQUID 400mg,10\n".getBytes()
        );

        List<OrderDocumentItemDTO> items = parser.parse(file);

        assertEquals(1, items.size());
        assertEquals("Brufen LIQUID 400mg", items.get(0).getVariantName());
        assertEquals(10, items.get(0).getRequestedQuantity());
    }

    @Test
    void csvParserParsesStructuredProductDocument() throws Exception {
        CsvOrderDocumentParser parser = new CsvOrderDocumentParser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "order.csv",
                "text/csv",
                "productName,form,dosage,requestedQuantity\nBrufen,LIQUID,400mg,10\n".getBytes()
        );

        List<OrderDocumentItemDTO> items = parser.parse(file);

        assertEquals(1, items.size());
        assertEquals("Brufen", items.get(0).getProductName());
        assertEquals("LIQUID", items.get(0).getForm());
        assertEquals("400mg", items.get(0).getDosage());
        assertEquals(10, items.get(0).getRequestedQuantity());
    }

    @Test
    void resolverRejectsUnsupportedFormat() {
        OrderDocumentParserResolver resolver = new OrderDocumentParserResolver(List.of(new CsvOrderDocumentParser()));
        MockMultipartFile file = new MockMultipartFile("file", "order.txt", "text/plain", "demo".getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> resolver.resolve(file));

        assertEquals("Only CSV procurement documents are currently supported.", exception.getMessage());
    }

    @Test
    void resolverRejectsJsonUploadWithCsvOnlyMessage() {
        OrderDocumentParserResolver resolver = new OrderDocumentParserResolver(List.of(
                new JsonOrderDocumentParser(new ObjectMapper()),
                new CsvOrderDocumentParser()
        ));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "order.json",
                "application/json",
                "[{\"variantId\":2,\"requestedQuantity\":10}]".getBytes()
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> resolver.resolve(file));

        assertEquals("Only CSV procurement documents are currently supported.", exception.getMessage());
    }
}
