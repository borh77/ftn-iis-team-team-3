package com.example.iisdrugcrm.dto.sales.activity;

import com.example.iisdrugcrm.domain.sales.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ActivityRequestDTO {

    @NotNull
    private ActivityType type;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDateTime scheduledAt;

    public ActivityType getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }

    public void setType(ActivityType type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
}