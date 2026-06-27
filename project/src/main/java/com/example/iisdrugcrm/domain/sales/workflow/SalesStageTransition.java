package com.example.iisdrugcrm.domain.sales.workflow;

import jakarta.persistence.*;

@Entity
@Table(name = "sales_stage_transitions")
public class SalesStageTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private SalesWorkflow workflow;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stage_id", nullable = false)
    private SalesStageDefinition fromStage;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "to_stage_id", nullable = false)
    private SalesStageDefinition toStage;

    @Column(length = 100)
    private String conditionType;

    @Column(length = 500)
    private String conditionDescription;

    protected SalesStageTransition() {
    }

    public SalesStageTransition(
            SalesWorkflow workflow,
            SalesStageDefinition fromStage,
            SalesStageDefinition toStage,
            String conditionType,
            String conditionDescription
    ) {
        this.workflow = workflow;
        this.fromStage = fromStage;
        this.toStage = toStage;
        this.conditionType = conditionType;
        this.conditionDescription = conditionDescription;
    }

    public Long getId() { return id; }
    public SalesWorkflow getWorkflow() { return workflow; }
    public SalesStageDefinition getFromStage() { return fromStage; }
    public SalesStageDefinition getToStage() { return toStage; }
    public String getConditionType() { return conditionType; }
    public String getConditionDescription() { return conditionDescription; }
}