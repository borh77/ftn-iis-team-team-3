package com.example.iisdrugcrm.dto.pricelist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class CreatePricelistDTO {

    @NotNull
    private Long regionId;

    @NotBlank
    @Size(max = 120)
    private String customerSegment;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be an ISO 4217 code")
    private String currency;

    @NotNull
    private OffsetDateTime periodStart;

    @NotNull
    private OffsetDateTime periodEnd;

    @NotEmpty
    @Valid
    private List<PricelistItemDTO> items;

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getCustomerSegment() {
        return customerSegment;
    }

    public void setCustomerSegment(String customerSegment) {
        this.customerSegment = customerSegment;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public OffsetDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(OffsetDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public OffsetDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(OffsetDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public List<PricelistItemDTO> getItems() {
        return items;
    }

    public void setItems(List<PricelistItemDTO> items) {
        this.items = items;
    }

    @AssertTrue(message = "periodStart must be before periodEnd")
    public boolean isPeriodValid() {
        return periodStart != null && periodEnd != null && periodStart.isBefore(periodEnd);
    }

    public static class PricelistItemDTO {

        @NotNull
        @Positive
        private Long variantId;

        @NotBlank
        @Size(max = 255)
        private String variantName;

        @NotEmpty
        @Valid
        private List<QuantityThresholdDTO> thresholds;

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

        public List<QuantityThresholdDTO> getThresholds() {
            return thresholds;
        }

        public void setThresholds(List<QuantityThresholdDTO> thresholds) {
            this.thresholds = thresholds;
        }
    }

    public static class QuantityThresholdDTO {

        @Min(1)
        private int quantityFrom;

        @Positive
        private Integer quantityTo;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal price;

        public int getQuantityFrom() {
            return quantityFrom;
        }

        public void setQuantityFrom(int quantityFrom) {
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

        @AssertTrue(message = "quantityTo must be greater than or equal to quantityFrom")
        public boolean isRangeValid() {
            return quantityTo == null || quantityTo >= quantityFrom;
        }
    }
}