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
    private Long regionId;
    private String regionName;

    public LeadResponseDTO(Long id, String name, String email, String address, String source,
                        Integer score, LeadStatus status, LocalDateTime createdAt, LocalDateTime updatedAt,
                        Long regionId, String regionName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.source = source;
        this.score = score;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.regionId = regionId;
        this.regionName = regionName;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public Long getRegionId() { return regionId; }
    public String getRegionName() { return regionName; }
    public String getSource() { return source; }
    public Integer getScore() { return score; }
    public LeadStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}