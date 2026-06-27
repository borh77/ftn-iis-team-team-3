package com.example.iisdrugcrm.service.order;

import com.example.iisdrugcrm.dto.order.OrderDocumentItemDTO;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface OrderDocumentParser {

    boolean supports(MultipartFile file);

    List<OrderDocumentItemDTO> parse(MultipartFile file) throws IOException;
}
