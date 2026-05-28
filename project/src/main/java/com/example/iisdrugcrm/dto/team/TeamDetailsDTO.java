package com.example.iisdrugcrm.dto.team;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TeamDetailsDTO {

    private Long id;
    private String name;
    private Long leaderId;
    private String leaderUsername;
    private Set<Long> memberIds = new LinkedHashSet<>();
    private List<TeamMemberDTO> members = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderUsername() {
        return leaderUsername;
    }

    public void setLeaderUsername(String leaderUsername) {
        this.leaderUsername = leaderUsername;
    }

    public Set<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(Set<Long> memberIds) {
        this.memberIds = memberIds;
    }

    public List<TeamMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMemberDTO> members) {
        this.members = members;
    }
}