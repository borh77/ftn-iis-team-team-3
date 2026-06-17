package com.example.iisdrugcrm.dto.sales.lead;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class LeadRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Email
    private String email;

    @Size(max = 255)
    private String address;

    @Size(max = 100)
    private String source;

    @Min(0)
    private Integer score;

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getSource() { return source; }
    public Integer getScore() { return score; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setSource(String source) { this.source = source; }
    public void setScore(Integer score) { this.score = score; }
}