package com.example.iisdrugcrm.domain.sales;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String source;

    @Column(nullable = false)
    private Integer score = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status = LeadStatus.NEW;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Lead() {
    }

    public Lead(String name, String email, String address, String source, Integer score) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.source = source;
        this.score = score == null ? 0 : score;
        this.status = LeadStatus.NEW;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void update(String name, String email, String address, String source, Integer score) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.source = source;
        this.score = score == null ? 0 : score;
    }

    public void qualify() {
        if (status == LeadStatus.CONVERTED) {
            throw new IllegalStateException("Converted lead cannot be qualified again.");
        }
        status = LeadStatus.QUALIFIED;
    }

    public void convert() {
        if (status != LeadStatus.QUALIFIED) {
            throw new IllegalStateException("Only qualified lead can be converted.");
        }

        status = LeadStatus.CONVERTED;
    }    

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getSource() { return source; }
    public Integer getScore() { return score; }
    public LeadStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}