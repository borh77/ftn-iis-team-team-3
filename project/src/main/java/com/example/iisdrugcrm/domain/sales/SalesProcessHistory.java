package com.example.iisdrugcrm.domain.sales;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_process_history")
public class SalesProcessHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_process_id", nullable = false)
    private SalesProcess salesProcess;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesStage previousStage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesStage newStage;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    protected SalesProcessHistory() {
    }

    public SalesProcessHistory(SalesProcess salesProcess, SalesStage previousStage, SalesStage newStage) {
        this.salesProcess = salesProcess;
        this.previousStage = previousStage;
        this.newStage = newStage;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public SalesProcess getSalesProcess() {
        return salesProcess;
    }

    public SalesStage getPreviousStage() {
        return previousStage;
    }

    public SalesStage getNewStage() {
        return newStage;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}