package com.example.iisdrugcrm.domain.sales;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales_activities")
public class SalesActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ActivityType type;

    @Enumerated(EnumType.STRING)
    private ActivityStatus status;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private LocalDateTime scheduledAt;

    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_process_id")
    private SalesProcess salesProcess;

    protected SalesActivity() {}

    public SalesActivity(
            ActivityType type,
            String title,
            String description,
            LocalDateTime scheduledAt,
            SalesProcess salesProcess
    ) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.scheduledAt = scheduledAt;
        this.salesProcess = salesProcess;
        this.status = ActivityStatus.PLANNED;
    }

    public void complete() {
        this.status = ActivityStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public ActivityType getType() { return type; }
    public ActivityStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public SalesProcess getSalesProcess() { return salesProcess; }
}