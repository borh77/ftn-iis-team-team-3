package com.example.iisdrugcrm.dto.sales.communication;

import com.example.iisdrugcrm.domain.sales.CommunicationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CommunicationRequestDTO {

    @NotNull
    private CommunicationType type;

    @NotNull
    private LocalDateTime communicationDate;

    @NotBlank
    private String summary;

    public CommunicationType getType() { return type; }
    public LocalDateTime getCommunicationDate() { return communicationDate; }
    public String getSummary() { return summary; }

    public void setType(CommunicationType type) { this.type = type; }
    public void setCommunicationDate(LocalDateTime communicationDate) { this.communicationDate = communicationDate; }
    public void setSummary(String summary) { this.summary = summary; }
}