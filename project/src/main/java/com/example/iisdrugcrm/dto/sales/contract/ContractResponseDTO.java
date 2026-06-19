package com.example.iisdrugcrm.dto.sales.contract;

import com.example.iisdrugcrm.domain.sales.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ContractResponseDTO {

    private Long id;
    private String contractNumber;
    private Long offerId;
    private String offerNumber;
    private Long customerId;
    private String customerName;
    private Long salesProcessId;
    private String salesProcessTitle;
    private ContractStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalValue;
    private String terms;
    private LocalDateTime createdAt;
    private LocalDateTime signedAt;

    public ContractResponseDTO(Long id, String contractNumber, Long offerId, String offerNumber,
                               Long customerId, String customerName, Long salesProcessId,
                               String salesProcessTitle, ContractStatus status,
                               LocalDate startDate, LocalDate endDate, BigDecimal totalValue,
                               String terms, LocalDateTime createdAt, LocalDateTime signedAt) {
        this.id = id;
        this.contractNumber = contractNumber;
        this.offerId = offerId;
        this.offerNumber = offerNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.salesProcessId = salesProcessId;
        this.salesProcessTitle = salesProcessTitle;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalValue = totalValue;
        this.terms = terms;
        this.createdAt = createdAt;
        this.signedAt = signedAt;
    }

    public Long getId() { return id; }
    public String getContractNumber() { return contractNumber; }
    public Long getOfferId() { return offerId; }
    public String getOfferNumber() { return offerNumber; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public Long getSalesProcessId() { return salesProcessId; }
    public String getSalesProcessTitle() { return salesProcessTitle; }
    public ContractStatus getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getTotalValue() { return totalValue; }
    public String getTerms() { return terms; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSignedAt() { return signedAt; }
}