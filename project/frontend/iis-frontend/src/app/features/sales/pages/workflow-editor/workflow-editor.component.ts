import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { RegionService } from '../../../../core/region.service';
import { Region } from '../../../../core/region.model';
import { SalesApiService } from '../../api/sales-api.service';
import {
  CreateSalesStageRequest,
  CreateSalesStageTransitionRequest,
  CreateSalesWorkflowRequest,
  SalesStageDefinition,
  SalesStageTransition,
  SalesWorkflow,
} from '../../models/sales-workflow.model';

@Component({
  selector: 'app-workflow-editor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './workflow-editor.component.html',
  styleUrls: ['./workflow-editor.component.css'],
})
export class WorkflowEditorComponent implements OnInit {

  private readonly salesApi = inject(SalesApiService);
  private readonly regionService = inject(RegionService);
  private readonly cdr = inject(ChangeDetectorRef);

  workflows: SalesWorkflow[] = [];
  selectedWorkflow: SalesWorkflow | null = null;

  stages: SalesStageDefinition[] = [];
  transitions: SalesStageTransition[] = [];
  regions: Region[] = [];

  loading = false;
  loadingWorkflowDetails = false;
  showStageForm = true;
  showTransitionForm = true;

  predefinedStages = [
    'New',
    'Contacted',
    'Qualified',
    'Proposal Sent',
    'Negotiation',
    'Closed Won',
    'Closed Lost',
  ];

  stageMode: 'PREDEFINED' | 'CUSTOM' = 'PREDEFINED';
  selectedPredefinedStage = '';

  newWorkflow: CreateSalesWorkflowRequest = {
    name: '',
    regionId: null,
  };

  newStage: CreateSalesStageRequest = {
    name: '',
    description: '',
    stageOrder: 1,
    startStage: false,
    endStage: false,
    successfulEnd: false,
    requiredInputs: '',
    expectedOutputs: '',
  };

  newTransition: CreateSalesStageTransitionRequest = {
    fromStageId: 0,
    toStageId: 0,
    conditionType: 'ALWAYS',
    conditionDescription: '',
  };

  ngOnInit(): void {
    this.loadRegions();
    this.loadWorkflows();
  }

  loadWorkflows(): void {
    this.loading = true;
    const selectedWorkflowId = this.selectedWorkflow?.id ?? null;

    this.salesApi.getSalesWorkflows().subscribe({
      next: workflows => {
        this.workflows = workflows ?? [];
        this.loading = false;

        this.selectWorkflowFromList(selectedWorkflowId);
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to load sales workflows:', err);
        this.loading = false;
        this.selectedWorkflow = null;
        this.stages = [];
        this.transitions = [];
        this.cdr.detectChanges();
      },
    });
  }

  selectWorkflow(workflow: SalesWorkflow): void {
    this.selectedWorkflow = workflow;
    this.cdr.detectChanges();
    this.refreshSelectedWorkflowDetails();
  }

  refreshSelectedWorkflowDetails(): void {
    if (!this.selectedWorkflow) {
      this.stages = [];
      this.transitions = [];
      this.loadingWorkflowDetails = false;
      this.cdr.detectChanges();
      return;
    }

    const workflow = this.selectedWorkflow;
    this.loadingWorkflowDetails = true;

    this.refreshStages(workflow.id);
    this.refreshTransitions(workflow.id);
  }

  refreshStages(workflowId = this.selectedWorkflow?.id): void {
    if (!workflowId) {
      this.stages = [];
      return;
    }

    this.salesApi.getSalesWorkflowStages(workflowId).subscribe({
      next: stages => {
        if (this.selectedWorkflow?.id === workflowId) {
          this.stages = this.sortStages(stages ?? []);
        }

        this.loadingWorkflowDetails = false;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error(err);
        this.loadingWorkflowDetails = false;
        this.cdr.detectChanges();
      },
    });
  }

  refreshTransitions(workflowId = this.selectedWorkflow?.id): void {
    if (!workflowId) {
      this.transitions = [];
      this.cdr.detectChanges();
      return;
    }

    this.salesApi.getSalesWorkflowTransitions(workflowId).subscribe({
      next: transitions => {
        if (this.selectedWorkflow?.id === workflowId) {
          this.transitions = transitions ?? [];
        }
        this.cdr.detectChanges();
      },
      error: err => {
        console.error(err);
        this.cdr.detectChanges();
      },
    });
  }

  createWorkflow(): void {
    this.salesApi.createSalesWorkflow(this.newWorkflow).subscribe({
      next: (workflow) => {
        this.newWorkflow = {
          name: '',
          regionId: null,
        };

        if (workflow?.id) {
          this.workflows = [
            ...this.workflows.filter(item => item.id !== workflow.id),
            workflow,
          ];
          this.selectWorkflow(workflow);
          this.refreshWorkflows(workflow.id);
          this.cdr.detectChanges();
          return;
        }

        this.refreshWorkflows(undefined, true);
      },
    });
  }

  createStage(): void {
    if (!this.selectedWorkflow) {
        return;
    }

    this.newStage.name = this.getStageName().trim();

    if (!this.isStageFormValid()) {
        return;
    }

    this.salesApi.addSalesWorkflowStage(
      this.selectedWorkflow.id,
      this.newStage,
    ).subscribe({
      next: () => {
        this.newStage = {
          name: '',
          description: '',
          stageOrder: 1,
          startStage: false,
          endStage: false,
          successfulEnd: false,
          requiredInputs: '',
          expectedOutputs: '',
        };
        this.stageMode = 'PREDEFINED';
        this.selectedPredefinedStage = '';
        this.showStageForm = true;

        this.refreshStages();
      },
    });
  }

  createTransition(): void {

    if (!this.selectedWorkflow || this.transitionInvalid) {
      return;
    }

    this.salesApi.addSalesWorkflowTransition(
      this.selectedWorkflow.id,
      this.newTransition,
    ).subscribe({
      next: () => {

        this.newTransition = {
          fromStageId: 0,
          toStageId: 0,
          conditionType: 'ALWAYS',
          conditionDescription: '',
        };

        this.showTransitionForm = true;
        this.refreshTransitions();
      },
    });
  }

  loadRegions(): void {
    this.regionService.list().subscribe({
        next: (response) => {
        this.regions = response ?? [];
        this.cdr.detectChanges();
        },
        error: (error) => {
        console.error('Failed to load regions:', error);
        this.cdr.detectChanges();
        },
    });
  }

  onStageModeChanged(mode: 'PREDEFINED' | 'CUSTOM'): void {
    this.stageMode = mode;
    this.selectedPredefinedStage = '';

    this.newStage.name = '';
  }

  onPredefinedStageChanged(stageName: string): void {
    this.selectedPredefinedStage = stageName;
    this.newStage.name = stageName;
    this.newStage.description = stageName ? `${stageName} stage` : '';
  }

  get nextStageOrder(): number {
    if (this.stages.length === 0) {
      return 1;
    }

    return Math.max(...this.stages.map((stage) => stage.stageOrder)) + 1;
  }

  useNextStageOrder(): void {
    this.newStage.stageOrder = this.nextStageOrder;
  }

  get transitionInvalid(): boolean {
    return (
      this.newTransition.fromStageId === 0 ||
      this.newTransition.toStageId === 0 ||
      this.newTransition.fromStageId === this.newTransition.toStageId
    );
  }

  getStageName(): string {
    return this.stageMode === 'PREDEFINED' ? this.selectedPredefinedStage : this.newStage.name;
  }

  isStageFormValid(): boolean {
    return this.getStageName().trim().length > 0;
  }

  getWorkflowRegionLabel(workflow: SalesWorkflow): string {
    return workflow.regionName || 'GLOBAL';
  }

  refreshSelectedWorkflow(): void {
    if (!this.selectedWorkflow) {
      return;
    }

    const workflowId = this.selectedWorkflow.id;
    const currentWorkflow = this.workflows.find(workflow => workflow.id === workflowId) ?? this.selectedWorkflow;
    this.selectWorkflow(currentWorkflow);
  }

  refreshWorkflows(preferredWorkflowId?: number, selectLatest = false): void {
    this.loading = true;
    const selectedWorkflowId = this.selectedWorkflow?.id ?? null;

    this.salesApi.getSalesWorkflows().subscribe({
      next: workflows => {
        this.workflows = workflows ?? [];
        this.loading = false;

        this.selectWorkflowFromList(selectedWorkflowId, preferredWorkflowId, selectLatest);
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to load sales workflows:', err);
        this.loading = false;
        this.selectedWorkflow = null;
        this.stages = [];
        this.transitions = [];
        this.cdr.detectChanges();
      },
    });
  }

  private selectWorkflowFromList(
    selectedWorkflowId: number | null,
    preferredWorkflowId?: number,
    selectLatest = false,
  ): void {
    const workflowToSelect =
      this.workflows.find(workflow => workflow.id === preferredWorkflowId) ??
      (selectLatest ? this.getLatestWorkflow(this.workflows) : null) ??
      this.workflows.find(workflow => workflow.id === selectedWorkflowId) ??
      this.workflows[0] ??
      null;

    if (workflowToSelect) {
      this.selectWorkflow(workflowToSelect);
      return;
    }

    this.selectedWorkflow = null;
    this.stages = [];
    this.transitions = [];
  }

  private sortStages(stages: SalesStageDefinition[]): SalesStageDefinition[] {
    return [...stages].sort((first, second) => first.stageOrder - second.stageOrder);
  }

  private getLatestWorkflow(workflows: SalesWorkflow[]): SalesWorkflow | null {
    if (workflows.length === 0) {
      return null;
    }

    return [...workflows].sort((first, second) => second.id - first.id)[0];
  }
}
