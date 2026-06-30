package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.order.ValidationResultDTO;
import com.example.iisdrugcrm.dto.procurement.ConfirmProcurementRequestDTO;
import com.example.iisdrugcrm.dto.procurement.ProcurementOrderResponseDTO;
import com.example.iisdrugcrm.service.OrderValidationService;
import com.example.iisdrugcrm.service.ProcurementOrderService;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ProcurementControllerTest {

    private OrderValidationService orderValidationService;
    private ProcurementOrderService procurementOrderService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderValidationService = mock(OrderValidationService.class);
        procurementOrderService = mock(ProcurementOrderService.class);
        mockMvc = standaloneSetup(new ProcurementController(orderValidationService, procurementOrderService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void validateOrderDocumentDelegatesToServiceWithAuthenticatedUsername() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "order.csv",
                "text/csv",
                "variantId,requestedQuantity\n2,10\n".getBytes()
        );
        ValidationResultDTO response = new ValidationResultDTO();
        response.setValid(true);
        when(orderValidationService.validateOrderDocument(eq("buyer"), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/procurement/validation")
                        .file(file)
                        .principal(new TestingAuthenticationToken("buyer", null))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid").value(true));

        verify(orderValidationService).validateOrderDocument(eq("buyer"), any());
    }

    @Test
    void jsonUploadReturnsCsvOnlyError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "order.json",
                "application/json",
                "[{\"variantId\":2,\"requestedQuantity\":10}]".getBytes()
        );
        when(orderValidationService.validateOrderDocument(eq("buyer"), any()))
                .thenThrow(new IllegalArgumentException("Only CSV procurement documents are currently supported."));

        mockMvc.perform(multipart("/api/procurement/validation")
                        .file(file)
                        .principal(new TestingAuthenticationToken("buyer", null))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Only CSV procurement documents are currently supported."));
    }

    @Test
    void endpointRequiresBuyerRole() throws Exception {
        Method method = ProcurementController.class.getMethod("validateOrderDocument", org.springframework.web.multipart.MultipartFile.class, org.springframework.security.core.Authentication.class);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertEquals("hasRole('BUYER')", preAuthorize.value());
    }

    @Test
    void confirmOrderDelegatesToServiceWithAuthenticatedUsername() throws Exception {
        ProcurementOrderResponseDTO response = new ProcurementOrderResponseDTO();
        response.setId(15L);
        when(procurementOrderService.confirm(eq("buyer"), any(ConfirmProcurementRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/procurement/orders")
                        .principal(new TestingAuthenticationToken("buyer", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceFileName\":\"order.csv\",\"items\":[{\"variantId\":2,\"requestedQuantity\":10}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15));

        verify(procurementOrderService).confirm(eq("buyer"), any(ConfirmProcurementRequestDTO.class));
    }

    @Test
    void listMyOrdersDelegatesToServiceWithAuthenticatedUsername() throws Exception {
        ProcurementOrderResponseDTO response = new ProcurementOrderResponseDTO();
        response.setId(15L);
        when(procurementOrderService.listMine("buyer")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/procurement/orders")
                        .principal(new TestingAuthenticationToken("buyer", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(15));

        verify(procurementOrderService).listMine("buyer");
    }

    @Test
    void getMyOrderDelegatesToServiceWithAuthenticatedUsername() throws Exception {
        ProcurementOrderResponseDTO response = new ProcurementOrderResponseDTO();
        response.setId(15L);
        when(procurementOrderService.getMine("buyer", 15L)).thenReturn(response);

        mockMvc.perform(get("/api/procurement/orders/15")
                        .principal(new TestingAuthenticationToken("buyer", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15));

        verify(procurementOrderService).getMine("buyer", 15L);
    }
}
