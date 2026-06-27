import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

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

  workflows: SalesWorkflow[] = [];
  selectedWorkflow: SalesWorkflow | null = null;

  stages: SalesStageDefinition[] = [];
  transitions: SalesStageTransition[] = [];

  loading = false;

  newWorkflow: CreateSalesWorkflowRequest = {
    name: '',
    region: 'GLOBAL',
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
    this.loadWorkflows();
  }

  loadWorkflows(): void {
    this.loading = true;

    this.salesApi.getSalesWorkflows().subscribe({
      next: workflows => {
        this.workflows = workflows;
        this.loading = false;

        if (workflows.length > 0) {
          this.selectWorkflow(workflows[0]);
        }
      },
      error: err => {
        console.error(err);
        this.loading = false;
      },
    });
  }

  selectWorkflow(workflow: SalesWorkflow): void {
    this.selectedWorkflow = workflow;

    this.salesApi.getSalesWorkflowStages(workflow.id).subscribe({
      next: stages => this.stages = stages,
    });

    this.salesApi.getSalesWorkflowTransitions(workflow.id).subscribe({
      next: transitions => this.transitions = transitions,
    });
  }

  createWorkflow(): void {
    this.salesApi.createSalesWorkflow(this.newWorkflow).subscribe({
      next: () => {
        this.newWorkflow = {
          name: '',
          region: 'GLOBAL',
        };

        this.loadWorkflows();
      },
    });
  }

  createStage(): void {

    if (!this.selectedWorkflow) {
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

        this.selectWorkflow(this.selectedWorkflow!);
      },
    });
  }

  createTransition(): void {

    if (!this.selectedWorkflow) {
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

        this.selectWorkflow(this.selectedWorkflow!);
      },
    });
  }
}