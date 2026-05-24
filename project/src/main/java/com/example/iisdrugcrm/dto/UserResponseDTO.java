package com.example.iisdrugcrm.dto;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;

public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private boolean active;
    private boolean hasChangedPassword;

    public static UserResponseDTO fromEntity(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setHasChangedPassword(user.isHasChangedPassword());
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isHasChangedPassword() {
        return hasChangedPassword;
    }

    public void setHasChangedPassword(boolean hasChangedPassword) {
        this.hasChangedPassword = hasChangedPassword;
    }
}