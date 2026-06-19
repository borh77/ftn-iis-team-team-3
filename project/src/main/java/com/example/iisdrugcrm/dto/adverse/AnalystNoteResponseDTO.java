package com.example.iisdrugcrm.dto.adverse;

import java.time.LocalDateTime;

public class AnalystNoteResponseDTO {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String authorUsername;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }
}

