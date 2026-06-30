package com.example.iisdrugcrm.dto.pricelist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public class SaveItemsStepDTO {

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<PricelistWizardItemDTO> items;

    public List<PricelistWizardItemDTO> getItems() {
        return items;
    }

    public void setItems(List<PricelistWizardItemDTO> items) {
        this.items = items;
    }

    public static class PricelistWizardItemDTO {

        @NotNull(message = "Variant is required")
        @Positive(message = "Variant id must be positive")
        private Long variantId;

        @NotBlank(message = "Variant name is required")
        private String variantName;

        public Long getVariantId() {
            return variantId;
        }

        public void setVariantId(Long variantId) {
            this.variantId = variantId;
        }

        public String getVariantName() {
            return variantName;
        }

        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }
    }
}
