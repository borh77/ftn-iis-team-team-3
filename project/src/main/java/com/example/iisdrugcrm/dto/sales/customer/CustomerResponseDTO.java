package com.example.iisdrugcrm.dto.sales.customer;

import com.example.iisdrugcrm.domain.sales.CustomerStatus;
import java.time.LocalDateTime;

public class CustomerResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String website;
    private String address;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerResponseDTO(Long id, String name, String email, String phone, String website,
                               String address, CustomerStatus status,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.website = website;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getWebsite() { return website; }
    public String getAddress() { return address; }
    public CustomerStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}