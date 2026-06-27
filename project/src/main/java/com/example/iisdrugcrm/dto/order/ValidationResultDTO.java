package com.example.iisdrugcrm.dto.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ValidationResultDTO {

    private boolean valid;
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private List<ValidatedOrderItemDTO> validatedItems = new ArrayList<>();
    private List<InvalidOrderItemDTO> invalidItems = new ArrayList<>();
    private List<ReplacementSuggestionDTO> replacements = new ArrayList<>();

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<ValidatedOrderItemDTO> getValidatedItems() {
        return validatedItems;
    }

    public void setValidatedItems(List<ValidatedOrderItemDTO> validatedItems) {
        this.validatedItems = validatedItems;
    }

    public List<InvalidOrderItemDTO> getInvalidItems() {
        return invalidItems;
    }

    public void setInvalidItems(List<InvalidOrderItemDTO> invalidItems) {
        this.invalidItems = invalidItems;
    }

    public List<ReplacementSuggestionDTO> getReplacements() {
        return replacements;
    }

    public void setReplacements(List<ReplacementSuggestionDTO> replacements) {
        this.replacements = replacements;
    }
}
