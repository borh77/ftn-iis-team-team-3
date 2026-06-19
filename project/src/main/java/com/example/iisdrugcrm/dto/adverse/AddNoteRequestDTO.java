package com.example.iisdrugcrm.dto.adverse;

import jakarta.validation.constraints.NotBlank;

public class AddNoteRequestDTO {

    @NotBlank
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

