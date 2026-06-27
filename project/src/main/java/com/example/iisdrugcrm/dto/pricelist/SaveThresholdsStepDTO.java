package com.example.iisdrugcrm.dto.pricelist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public class SaveThresholdsStepDTO {

    @NotEmpty(message = "Thresholds are required")
    @Valid
    private List<PricelistItemThresholdsDTO> items;

    public List<PricelistItemThresholdsDTO> getItems() {
        return items;
    }

    public void setItems(List<PricelistItemThresholdsDTO> items) {
        this.items = items;
    }

    public static class PricelistItemThresholdsDTO {

        @NotNull(message = "Variant is required")
        @Positive(message = "Variant id must be positive")
        private Long variantId;

        @NotEmpty(message = "Each item must have thresholds")
        @Valid
        private List<QuantityThresholdDTO> thresholds;

        public Long getVariantId() {
            return variantId;
        }

        public void setVariantId(Long variantId) {
            this.variantId = variantId;
        }

        public List<QuantityThresholdDTO> getThresholds() {
            return thresholds;
        }

        public void setThresholds(List<QuantityThresholdDTO> thresholds) {
            this.thresholds = thresholds;
        }
    }

    public static class QuantityThresholdDTO {

        @NotNull(message = "Quantity from is required")
        @Min(value = 1, message = "Quantity from must be at least 1")
        private Integer quantityFrom;

        @Positive(message = "Quantity to must be positive")
        private Integer quantityTo;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        private BigDecimal price;

        public Integer getQuantityFrom() {
            return quantityFrom;
        }

        public void setQuantityFrom(Integer quantityFrom) {
            this.quantityFrom = quantityFrom;
        }

        public Integer getQuantityTo() {
            return quantityTo;
        }

        public void setQuantityTo(Integer quantityTo) {
            this.quantityTo = quantityTo;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
