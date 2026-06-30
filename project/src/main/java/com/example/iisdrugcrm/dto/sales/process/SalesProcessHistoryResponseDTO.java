package com.example.iisdrugcrm.dto.sales.process;

import java.time.LocalDateTime;

public class SalesProcessHistoryResponseDTO {
    private Long id;
    private Long salesProcessId;
    private String previousStage;
    private String newStage;
    private LocalDateTime changedAt;
    private Long changedById;
    private String changedByUsername;

    public SalesProcessHistoryResponseDTO(
            Long id,
            Long salesProcessId,
            String previousStage,
            String newStage,
            LocalDateTime changedAt,
            Long changedById,
            String changedByUsername
    ) {
        this.id = id;
        this.salesProcessId = salesProcessId;
        this.previousStage = previousStage;
        this.newStage = newStage;
        this.changedAt = changedAt;
        this.changedById = changedById;
        this.changedByUsername = changedByUsername;
    }

    public Long getId() { return id; }
    public Long getSalesProcessId() { return salesProcessId; }
    public String getPreviousStage() { return previousStage; }
    public String getNewStage() { return newStage; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public Long getChangedById() { return changedById; }
    public String getChangedByUsername() { return changedByUsername; }
}