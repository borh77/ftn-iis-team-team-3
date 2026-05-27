package com.example.iisdrugcrm.dto.sales.lead;

import com.example.iisdrugcrm.domain.sales.LeadStatus;

import java.time.LocalDateTime;

public class LeadResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String address;
    private String source;
    private Integer score;
    private LeadStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LeadResponseDTO(Long id, String name, String email, String address, String source,
                           Integer score, LeadStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.source = source;
        this.score = score;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getSource() { return source; }
    public Integer getScore() { return score; }
    public LeadStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}