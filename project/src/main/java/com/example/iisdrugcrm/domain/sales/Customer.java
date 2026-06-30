package com.example.iisdrugcrm.domain.sales;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.iisdrugcrm.domain.Region;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    protected Customer() {
    }

    public Customer(String name, String email, String phone, String website, String address, Region region) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.website = website;
        this.address = address;
        this.region = region;
        this.status = CustomerStatus.ACTIVE;
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

    public void update(String name, String email, String phone, String website, String address, Region region) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.website = website;
        this.address = address;
        this.region = region;
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
    public Region getRegion() { return region; }
}