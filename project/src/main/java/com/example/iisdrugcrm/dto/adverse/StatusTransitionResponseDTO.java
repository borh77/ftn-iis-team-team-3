package com.example.iisdrugcrm.dto.adverse;

import java.time.LocalDateTime;

public class StatusTransitionResponseDTO {

    private Long id;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changedAt;
    private String changedByUsername;
    private String comment;
    private String priority;
    private String closureReason;
    private String verdict;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getChangedByUsername() { return changedByUsername; }
    public void setChangedByUsername(String changedByUsername) { this.changedByUsername = changedByUsername; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getClosureReason() { return closureReason; }
    public void setClosureReason(String closureReason) { this.closureReason = closureReason; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }
}

