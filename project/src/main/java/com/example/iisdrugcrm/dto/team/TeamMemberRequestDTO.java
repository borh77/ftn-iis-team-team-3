package com.example.iisdrugcrm.dto.team;

import jakarta.validation.constraints.NotNull;

public class TeamMemberRequestDTO {

    @NotNull
    private Long memberId;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}