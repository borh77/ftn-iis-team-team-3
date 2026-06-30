package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.service.PricelistService;
import com.example.iisdrugcrm.service.PricelistWizardService;
import com.example.iisdrugcrm.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PricelistControllerValidationTest {

    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        UserService userService = mock(UserService.class);
        PricelistController controller = new PricelistController(mock(PricelistService.class), userService);
        PricelistWizardController wizardController = new PricelistWizardController(mock(PricelistWizardService.class), userService);
        mockMvc = standaloneSetup(controller, wizardController)
                .setControllerAdvice(new RestExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void createFailsWhenFinalQuantityThresholdIsNotOpenEnded() throws Exception {
        assertInvalidThresholds("""
                [
                  {
                    "quantityFrom": 1,
                    "quantityTo": 10,
                    "price": 100
                  },
                  {
                    "quantityFrom": 11,
                    "quantityTo": 50,
                    "price": 95
                  }
                ]
                """, "Final quantity threshold must be open-ended");
    }

    @Test
    void createFailsWhenQuantityThresholdsHaveGap() throws Exception {
        assertInvalidThresholds("""
                [
                  {
                    "quantityFrom": 1,
                    "quantityTo": 10,
                    "price": 100
                  },
                  {
                    "quantityFrom": 12,
                    "quantityTo": null,
                    "price": 95
                  }
                ]
                """, "Quantity thresholds must be consecutive without gaps or overlaps");
    }

    @Test
    void createFailsWhenQuantityThresholdsOverlap() throws Exception {
        assertInvalidThresholds("""
                [
                  {
                    "quantityFrom": 1,
                    "quantityTo": 10,
                    "price": 100
                  },
                  {
                    "quantityFrom": 10,
                    "quantityTo": null,
                    "price": 95
                  }
                ]
                """, "Quantity thresholds must be consecutive without gaps or overlaps");
    }

    @Test
    void createFailsWhenHigherQuantityThresholdIsMoreExpensive() throws Exception {
        assertInvalidThresholds("""
                [
                  {
                    "quantityFrom": 1,
                    "quantityTo": 10,
                    "price": 100
                  },
                  {
                    "quantityFrom": 11,
                    "quantityTo": null,
                    "price": 110
                  }
                ]
                """, "Higher quantity thresholds cannot be more expensive");
    }

    @Test
    void createDtoPassesWhenFinalQuantityThresholdIsOpenEnded() {
        Set<ConstraintViolation<CreatePricelistDTO>> violations = getValidator().validate(validDto());

        assertTrue(violations.isEmpty());
    }

    @Test
    void wizardBasicInfoValidationReturnsClearError() throws Exception {
        mockMvc.perform(put("/api/cenovnici/100/wizard/basic-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerSegment": "Lanci apoteka",
                                  "currency": "RSD",
                                  "periodStart": "2026-07-01T00:00:00Z",
                                  "periodEnd": "2026-09-30T00:00:00Z"
                                }
                                """))
                .andExpect(status().is(422))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Region is required"));
    }

    private void assertInvalidThresholds(String thresholdsJson, String expectedMessage) throws Exception {
        mockMvc.perform(post("/api/pricelists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "regionId": 1,
                                  "customerSegment": "Lanci apoteka",
                                  "currency": "RSD",
                                  "periodStart": "2026-07-01T00:00:00Z",
                                  "periodEnd": "2026-09-30T00:00:00Z",
                                  "items": [
                                    {
                                      "variantId": 10,
                                      "variantName": "Variant A",
                                      "thresholds": %s
                                    }
                                  ]
                                }
                                """.formatted(thresholdsJson)))
                .andExpect(status().is(422))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(expectedMessage));
    }

    private Validator getValidator() {
        return validator;
    }

    private CreatePricelistDTO validDto() {
        CreatePricelistDTO dto = new CreatePricelistDTO();
        dto.setRegionId(1L);
        dto.setCustomerSegment("Lanci apoteka");
        dto.setCurrency("RSD");
        dto.setPeriodStart(OffsetDateTime.of(2026, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        dto.setPeriodEnd(OffsetDateTime.of(2026, 9, 30, 0, 0, 0, 0, ZoneOffset.UTC));

        CreatePricelistDTO.PricelistItemDTO item = new CreatePricelistDTO.PricelistItemDTO();
        item.setVariantId(10L);
        item.setVariantName("Variant A");
        item.setThresholds(List.of(
                threshold(1, 10, "100.00"),
                threshold(11, null, "95.00")
        ));
        dto.setItems(List.of(item));
        return dto;
    }

    private CreatePricelistDTO.QuantityThresholdDTO threshold(int quantityFrom, Integer quantityTo, String price) {
        CreatePricelistDTO.QuantityThresholdDTO threshold = new CreatePricelistDTO.QuantityThresholdDTO();
        threshold.setQuantityFrom(quantityFrom);
        threshold.setQuantityTo(quantityTo);
        threshold.setPrice(new BigDecimal(price));
        return threshold;
    }
}
