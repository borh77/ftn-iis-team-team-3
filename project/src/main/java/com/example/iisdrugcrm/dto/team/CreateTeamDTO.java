package com.example.iisdrugcrm.dto.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateTeamDTO {

    @NotBlank
    @Size(max = 120)
    private String teamName;

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}