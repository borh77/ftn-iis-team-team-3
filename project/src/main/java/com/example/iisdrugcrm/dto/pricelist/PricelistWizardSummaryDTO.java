package com.example.iisdrugcrm.dto.pricelist;

import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import java.util.List;

public class PricelistWizardSummaryDTO {

    private Long pricelistId;
    private boolean readyToFinish;
    private List<String> validationMessages;
    private PricelistResponseDTO pricelist;

    public static PricelistWizardSummaryDTO of(Pricelist pricelist, PricelistResponseDTO response, List<String> validationMessages) {
        PricelistWizardSummaryDTO dto = new PricelistWizardSummaryDTO();
        dto.setPricelistId(pricelist.getId());
        dto.setPricelist(response);
        dto.setValidationMessages(validationMessages);
        dto.setReadyToFinish(validationMessages == null || validationMessages.isEmpty());
        return dto;
    }

    public Long getPricelistId() {
        return pricelistId;
    }

    public void setPricelistId(Long pricelistId) {
        this.pricelistId = pricelistId;
    }

    public boolean isReadyToFinish() {
        return readyToFinish;
    }

    public void setReadyToFinish(boolean readyToFinish) {
        this.readyToFinish = readyToFinish;
    }

    public List<String> getValidationMessages() {
        return validationMessages;
    }

    public void setValidationMessages(List<String> validationMessages) {
        this.validationMessages = validationMessages;
    }

    public PricelistResponseDTO getPricelist() {
        return pricelist;
    }

    public void setPricelist(PricelistResponseDTO pricelist) {
        this.pricelist = pricelist;
    }
}
