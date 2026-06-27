package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.dto.pricelist.PromotionSuggestionDTO;
import com.example.iisdrugcrm.service.PromotionSuggestionService;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PromotionControllerTest {

    private PromotionSuggestionService promotionSuggestionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        promotionSuggestionService = mock(PromotionSuggestionService.class);
        mockMvc = standaloneSetup(new PromotionController(promotionSuggestionService)).build();
    }

    @Test
    void suggestionsEndpointReturnsServiceResults() throws Exception {
        PromotionSuggestionDTO suggestion = new PromotionSuggestionDTO();
        suggestion.setVariantId(101L);
        suggestion.setTargetName("Medicine A");
        suggestion.setCustomerSegment("Pharmacy chains");
        suggestion.setSuggestedDiscountType(DiscountType.PERCENTAGE);
        suggestion.setSuggestedDiscountValue(new BigDecimal("10.00"));
        suggestion.setReason("Reason");
        suggestion.setSource("ACTIVE_PRICELIST_HEURISTIC");
        when(promotionSuggestionService.getSuggestions("Pharmacy chains")).thenReturn(List.of(suggestion));

        mockMvc.perform(get("/api/promotions/suggestions").param("segment", "Pharmacy chains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].variantId").value(101))
                .andExpect(jsonPath("$[0].targetName").value("Medicine A"))
                .andExpect(jsonPath("$[0].suggestedDiscountType").value("PERCENTAGE"))
                .andExpect(jsonPath("$[0].suggestedDiscountValue").value(10.00))
                .andExpect(jsonPath("$[0].source").value("ACTIVE_PRICELIST_HEURISTIC"));

        verify(promotionSuggestionService).getSuggestions("Pharmacy chains");
    }

    @Test
    void controllerRequiresPricelistCreatorRole() {
        PreAuthorize preAuthorize = PromotionController.class.getAnnotation(PreAuthorize.class);

        assertEquals("hasRole('PRICELIST_CREATOR')", preAuthorize.value());
    }

    @Test
    void suggestionsEndpointMappingExists() throws Exception {
        Method method = PromotionController.class.getMethod("getSuggestions", String.class);

        assertEquals("getSuggestions", method.getName());
    }
}
