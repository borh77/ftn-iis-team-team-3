package com.example.iisdrugcrm.domain.sales;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_needs")
public class CustomerNeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_process_id", nullable = false)
    private SalesProcess salesProcess;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(length = 50)
    private String priority;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected CustomerNeed() {
    }

    public CustomerNeed(Customer customer, SalesProcess salesProcess, String description, String priority) {
        this.customer = customer;
        this.salesProcess = salesProcess;
        this.description = description;
        this.priority = priority;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public SalesProcess getSalesProcess() {
        return salesProcess;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}