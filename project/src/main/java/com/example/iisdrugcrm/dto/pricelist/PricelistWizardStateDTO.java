package com.example.iisdrugcrm.dto.pricelist;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistCreationStep;
import java.time.OffsetDateTime;

public class PricelistWizardStateDTO {

    private Long pricelistId;
    private PricelistCreationStep creationStep;
    private boolean creationCompleted;
    private PricelistStatus status;
    private Long teamId;
    private String teamName;
    private OffsetDateTime lastEditedAt;
    private PricelistResponseDTO pricelist;

    public static PricelistWizardStateDTO fromEntity(Pricelist pricelist, PricelistResponseDTO response) {
        PricelistWizardStateDTO dto = new PricelistWizardStateDTO();
        dto.setPricelistId(pricelist.getId());
        dto.setCreationStep(pricelist.getCreationStep());
        dto.setCreationCompleted(pricelist.isCreationCompleted());
        dto.setStatus(pricelist.getStatus());
        dto.setLastEditedAt(pricelist.getLastEditedAt());
        dto.setPricelist(response);
        if (pricelist.getTeam() != null) {
            dto.setTeamId(pricelist.getTeam().getId());
            dto.setTeamName(pricelist.getTeam().getName());
        }
        return dto;
    }

    public Long getPricelistId() {
        return pricelistId;
    }

    public void setPricelistId(Long pricelistId) {
        this.pricelistId = pricelistId;
    }

    public PricelistCreationStep getCreationStep() {
        return creationStep;
    }

    public void setCreationStep(PricelistCreationStep creationStep) {
        this.creationStep = creationStep;
    }

    public boolean isCreationCompleted() {
        return creationCompleted;
    }

    public void setCreationCompleted(boolean creationCompleted) {
        this.creationCompleted = creationCompleted;
    }

    public PricelistStatus getStatus() {
        return status;
    }

    public void setStatus(PricelistStatus status) {
        this.status = status;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public OffsetDateTime getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(OffsetDateTime lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

    public PricelistResponseDTO getPricelist() {
        return pricelist;
    }

    public void setPricelist(PricelistResponseDTO pricelist) {
        this.pricelist = pricelist;
    }
}
