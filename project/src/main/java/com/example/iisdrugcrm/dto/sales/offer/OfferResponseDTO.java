package com.example.iisdrugcrm.dto.sales.offer;

import com.example.iisdrugcrm.domain.sales.OfferStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OfferResponseDTO {

    private Long id;
    private String offerNumber;
    private Long customerId;
    private String customerName;
    private Long salesProcessId;
    private String salesProcessTitle;
    private OfferStatus status;
    private LocalDate validUntil;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private List<OfferItemResponseDTO> items;

    public OfferResponseDTO(Long id, String offerNumber, Long customerId, String customerName,
                            Long salesProcessId, String salesProcessTitle, OfferStatus status,
                            LocalDate validUntil, BigDecimal totalAmount, String notes,
                            LocalDateTime createdAt, List<OfferItemResponseDTO> items) {
        this.id = id;
        this.offerNumber = offerNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.salesProcessId = salesProcessId;
        this.salesProcessTitle = salesProcessTitle;
        this.status = status;
        this.validUntil = validUntil;
        this.totalAmount = totalAmount;
        this.notes = notes;
        this.createdAt = createdAt;
        this.items = items;
    }

    public Long getId() { return id; }
    public String getOfferNumber() { return offerNumber; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public Long getSalesProcessId() { return salesProcessId; }
    public String getSalesProcessTitle() { return salesProcessTitle; }
    public OfferStatus getStatus() { return status; }
    public LocalDate getValidUntil() { return validUntil; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OfferItemResponseDTO> getItems() { return items; }
}