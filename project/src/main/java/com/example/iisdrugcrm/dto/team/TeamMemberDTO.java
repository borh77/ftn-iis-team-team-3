package com.example.iisdrugcrm.dto.team;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;

public class TeamMemberDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private UserRole role;

    public static TeamMemberDTO fromEntity(User user) {
        TeamMemberDTO dto = new TeamMemberDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}