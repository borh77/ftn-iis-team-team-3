package com.example.iisdrugcrm.domain.sales;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.iisdrugcrm.domain.User;

@Entity
@Table(name = "sales_process_history")
public class SalesProcessHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_process_id", nullable = false)
    private SalesProcess salesProcess;

    @Column(nullable = false, length = 100)
    private String previousStage;

    @Column(nullable = false, length = 100)
    private String newStage;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id")
    private User changedBy;

    protected SalesProcessHistory() {
    }

    public SalesProcessHistory(SalesProcess salesProcess, String previousStage, String newStage, User changedBy) {
        this.salesProcess = salesProcess;
        this.previousStage = previousStage;
        this.newStage = newStage;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public SalesProcess getSalesProcess() {
        return salesProcess;
    }

    public String getPreviousStage() { 
        return previousStage; 
    }

    public String getNewStage() { 
        return newStage; 
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public User getChangedBy() {
        return changedBy;
    }
}