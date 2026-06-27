package com.example.iisdrugcrm.dto.sales.process;

import com.example.iisdrugcrm.domain.sales.SalesStage;
import java.time.LocalDateTime;

public class SalesProcessHistoryResponseDTO {
    private Long id;
    private Long salesProcessId;
    private SalesStage previousStage;
    private SalesStage newStage;
    private LocalDateTime changedAt;
    private Long changedById;
    private String changedByUsername;

    public SalesProcessHistoryResponseDTO(
            Long id,
            Long salesProcessId,
            SalesStage previousStage,
            SalesStage newStage,
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
    public SalesStage getPreviousStage() { return previousStage; }
    public SalesStage getNewStage() { return newStage; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public Long getChangedById() { return changedById; }
    public String getChangedByUsername() { return changedByUsername; }
}