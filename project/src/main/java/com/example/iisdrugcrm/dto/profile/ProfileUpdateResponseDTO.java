package com.example.iisdrugcrm.dto.profile;

import com.example.iisdrugcrm.domain.UserRole;
import java.util.List;

public class ProfileUpdateResponseDTO {

    private Long id;
    private String token;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private List<UserRole> roles;
    private boolean active;
    private boolean hasChangedPassword;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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