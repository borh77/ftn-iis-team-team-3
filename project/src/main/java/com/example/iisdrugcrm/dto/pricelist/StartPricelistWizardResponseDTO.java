package com.example.iisdrugcrm.dto.pricelist;

public class StartPricelistWizardResponseDTO {

    private Long pricelistId;
    private PricelistWizardStateDTO state;

    public StartPricelistWizardResponseDTO(Long pricelistId, PricelistWizardStateDTO state) {
        this.pricelistId = pricelistId;
        this.state = state;
    }

    public Long getPricelistId() {
        return pricelistId;
    }

    public void setPricelistId(Long pricelistId) {
        this.pricelistId = pricelistId;
    }

    public PricelistWizardStateDTO getState() {
        return state;
    }

    public void setState(PricelistWizardStateDTO state) {
        this.state = state;
    }
}
