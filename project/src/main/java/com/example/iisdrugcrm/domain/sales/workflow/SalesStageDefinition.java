package com.example.iisdrugcrm.domain.sales.workflow;

import jakarta.persistence.*;

@Entity
@Table(name = "sales_stage_definitions")
public class SalesStageDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private SalesWorkflow workflow;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer stageOrder;

    @Column(nullable = false)
    private boolean startStage;

    @Column(nullable = false)
    private boolean endStage;

    @Column(nullable = false)
    private boolean successfulEnd;

    @Column(columnDefinition = "TEXT")
    private String requiredInputs;

    @Column(columnDefinition = "TEXT")
    private String expectedOutputs;

    protected SalesStageDefinition() {
    }

    public SalesStageDefinition(
            SalesWorkflow workflow,
            String name,
            String description,
            Integer stageOrder,
            boolean startStage,
            boolean endStage,
            boolean successfulEnd,
            String requiredInputs,
            String expectedOutputs
    ) {
        this.workflow = workflow;
        this.name = name;
        this.description = description;
        this.stageOrder = stageOrder;
        this.startStage = startStage;
        this.endStage = endStage;
        this.successfulEnd = successfulEnd;
        this.requiredInputs = requiredInputs;
        this.expectedOutputs = expectedOutputs;
    }

    public Long getId() { return id; }
    public SalesWorkflow getWorkflow() { return workflow; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getStageOrder() { return stageOrder; }
    public boolean isStartStage() { return startStage; }
    public boolean isEndStage() { return endStage; }
    public boolean isSuccessfulEnd() { return successfulEnd; }
    public String getRequiredInputs() { return requiredInputs; }
    public String getExpectedOutputs() { return expectedOutputs; }
}