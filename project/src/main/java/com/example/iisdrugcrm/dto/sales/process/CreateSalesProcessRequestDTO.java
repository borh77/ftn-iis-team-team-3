package com.example.iisdrugcrm.dto.sales.process;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateSalesProcessRequestDTO {

    @NotNull
    private Long customerId;

    @NotBlank
    @Size(max = 200)
    private String title;

    public Long getCustomerId() { return customerId; }
    public String getTitle() { return title; }

    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setTitle(String title) { this.title = title; }
}