package com.example.iisdrugcrm.dto.auth;

import com.example.iisdrugcrm.domain.UserRole;
import java.util.List;

public class LoginResponseDTO {

    private String token;
    private String username;
    private List<UserRole> roles;
    private boolean active;
    private boolean hasChangedPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(List<UserRole> roles) {
        this.roles = roles;
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