package com.example.iisdrugcrm.dto.adverse;

import jakarta.validation.constraints.NotBlank;

public class ChangeStatusRequestDTO {

    @NotBlank
    private String newStatus;

    private String comment;
    private String priority;
    private String closureReason;
    private String verdict;

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getClosureReason() { return closureReason; }
    public void setClosureReason(String closureReason) { this.closureReason = closureReason; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }
}

