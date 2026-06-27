package com.example.iisdrugcrm.dto.pricelist;

import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import java.time.OffsetDateTime;

public class PricelistActivityLogResponseDTO {

    private Long id;
    private Long pricelistId;
    private Long userId;
    private Long teamId;
    private PricelistActionType actionType;
    private String description;
    private OffsetDateTime timestamp;

    public static PricelistActivityLogResponseDTO fromEntity(PricelistActivityLog log) {
        PricelistActivityLogResponseDTO dto = new PricelistActivityLogResponseDTO();
        dto.setId(log.getId());
        dto.setPricelistId(log.getPricelistId());
        dto.setUserId(log.getUserId());
        dto.setTeamId(log.getTeamId());
        dto.setActionType(log.getActionType());
        dto.setDescription(log.getDescription());
        dto.setTimestamp(log.getTimestamp());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPricelistId() {
        return pricelistId;
    }

    public void setPricelistId(Long pricelistId) {
        this.pricelistId = pricelistId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public PricelistActionType getActionType() {
        return actionType;
    }

    public void setActionType(PricelistActionType actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
