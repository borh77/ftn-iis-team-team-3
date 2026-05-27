package com.example.iisdrugcrm.dto.sales.process;

import com.example.iisdrugcrm.domain.sales.SalesProcessOutcome;
import com.example.iisdrugcrm.domain.sales.SalesProcessStatus;

import java.time.LocalDateTime;

public class SalesProcessResponseDTO {

    private Long id;
    private Long customerId;
    private String customerName;
    private String title;
    private String currentStage;
    private SalesProcessStatus status;
    private SalesProcessOutcome outcome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SalesProcessResponseDTO(Long id, Long customerId, String customerName, String title,
                                   String currentStage, SalesProcessStatus status,
                                   SalesProcessOutcome outcome,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.title = title;
        this.currentStage = currentStage;
        this.status = status;
        this.outcome = outcome;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getTitle() { return title; }
    public String getCurrentStage() { return currentStage; }
    public SalesProcessStatus getStatus() { return status; }
    public SalesProcessOutcome getOutcome() { return outcome; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}