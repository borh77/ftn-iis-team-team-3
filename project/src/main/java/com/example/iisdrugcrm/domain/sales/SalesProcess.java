package com.example.iisdrugcrm.domain.sales;

import com.example.iisdrugcrm.domain.sales.workflow.SalesWorkflow;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales_processes")
public class SalesProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesProcessStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SalesProcessOutcome outcome;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 100)
    private String stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private SalesWorkflow workflow;

    protected SalesProcess() {
    }

    public SalesProcess(Customer customer, String title, SalesWorkflow workflow, String startStage) {
        this.customer = customer;
        this.title = title;
        this.workflow = workflow;
        this.status = SalesProcessStatus.ACTIVE;
        this.outcome = SalesProcessOutcome.OPEN;
        this.stage = startStage;
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

    public void changeStage(String newStage, boolean endStage, boolean successfulEnd) {
        this.stage = newStage;

        if (endStage && successfulEnd) {
            this.status = SalesProcessStatus.SUCCESSFUL;
            this.outcome = SalesProcessOutcome.CLOSED_WON;
        } else if (endStage) {
            this.status = SalesProcessStatus.UNSUCCESSFUL;
            this.outcome = SalesProcessOutcome.CLOSED_LOST;
        } else {
            this.status = SalesProcessStatus.ACTIVE;
            this.outcome = SalesProcessOutcome.OPEN;
        }
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public String getTitle() { return title; }
    public SalesProcessStatus getStatus() { return status; }
    public SalesProcessOutcome getOutcome() { return outcome; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getStage() { return stage; }
    public SalesWorkflow getWorkflow() { return workflow; }
}