package com.example.iisdrugcrm.dto.sales.offer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class CreateOfferRequestDTO {

    @NotNull
    private Long customerId;

    @NotNull
    private Long salesProcessId;

    @NotNull
    @Future
    private LocalDate validUntil;

    private String notes;

    @Valid
    @NotEmpty
    private List<OfferItemRequestDTO> items;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getSalesProcessId() { return salesProcessId; }
    public void setSalesProcessId(Long salesProcessId) { this.salesProcessId = salesProcessId; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<OfferItemRequestDTO> getItems() { return items; }
    public void setItems(List<OfferItemRequestDTO> items) { this.items = items; }
}