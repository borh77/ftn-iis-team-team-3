package com.example.iisdrugcrm.dto.sales.need;

import java.time.LocalDateTime;

public class CustomerNeedResponseDTO {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long salesProcessId;
    private String salesProcessTitle;
    private String description;
    private String priority;
    private LocalDateTime createdAt;

    public CustomerNeedResponseDTO(Long id, Long customerId, String customerName,
                                   Long salesProcessId, String salesProcessTitle,
                                   String description, String priority,
                                   LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.salesProcessId = salesProcessId;
        this.salesProcessTitle = salesProcessTitle;
        this.description = description;
        this.priority = priority;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Long getSalesProcessId() {
        return salesProcessId;
    }

    public String getSalesProcessTitle() {
        return salesProcessTitle;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}