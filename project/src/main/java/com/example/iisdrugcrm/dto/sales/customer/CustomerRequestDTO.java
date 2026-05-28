package com.example.iisdrugcrm.dto.sales.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Email
    private String email;

    @Size(max = 50)
    private String phone;

    @Size(max = 255)
    private String website;

    @Size(max = 255)
    private String address;

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getWebsite() { return website; }
    public String getAddress() { return address; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setWebsite(String website) { this.website = website; }
    public void setAddress(String address) { this.address = address; }
}