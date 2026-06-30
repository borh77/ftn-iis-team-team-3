package com.example.iisdrugcrm.dto.procurement;

import com.example.iisdrugcrm.domain.procurement.ProcurementOrder;
import com.example.iisdrugcrm.domain.procurement.ProcurementOrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class ProcurementOrderResponseDTO {

    private Long id;
    private ProcurementOrderStatus status;
    private String buyerName;
    private String buyerUsername;
    private String regionName;
    private String customerSegment;
    private Long pricelistId;
    private String sourceFileName;
    private BigDecimal totalPrice;
    private String currency;
    private OffsetDateTime createdAt;
    private OffsetDateTime confirmedAt;
    private List<ProcurementOrderItemResponseDTO> items;

    public static ProcurementOrderResponseDTO fromEntity(ProcurementOrder order) {
        ProcurementOrderResponseDTO dto = new ProcurementOrderResponseDTO();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setBuyerName(order.getBuyerDisplayName());
        dto.setBuyerUsername(order.getBuyerUsername());
        dto.setRegionName(order.getRegionName());
        dto.setCustomerSegment(order.getCustomerSegment());
        dto.setPricelistId(order.getPricelist() == null ? null : order.getPricelist().getId());
        dto.setSourceFileName(order.getSourceFileName());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setCurrency(order.getCurrency());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setConfirmedAt(order.getConfirmedAt());
        dto.setItems(order.getItems().stream().map(ProcurementOrderItemResponseDTO::fromEntity).toList());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProcurementOrderStatus getStatus() { return status; }
    public void setStatus(ProcurementOrderStatus status) { this.status = status; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerUsername() { return buyerUsername; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }
    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }
    public String getCustomerSegment() { return customerSegment; }
    public void setCustomerSegment(String customerSegment) { this.customerSegment = customerSegment; }
    public Long getPricelistId() { return pricelistId; }
    public void setPricelistId(Long pricelistId) { this.pricelistId = pricelistId; }
    public String getSourceFileName() { return sourceFileName; }
    public void setSourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(OffsetDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public List<ProcurementOrderItemResponseDTO> getItems() { return items; }
    public void setItems(List<ProcurementOrderItemResponseDTO> items) { this.items = items; }
}
