package com.example.iisdrugcrm.dto.sales.need;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CustomerNeedRequestDTO {

    @NotNull
    private Long salesProcessId;

    @NotBlank
    private String description;

    private String priority;

    public Long getSalesProcessId() {
        return salesProcessId;
    }

    public void setSalesProcessId(Long salesProcessId) {
        this.salesProcessId = salesProcessId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}