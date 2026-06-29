import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PricelistTeam } from '../../core/team.models';
import { TeamService } from '../../core/team.service';
import { ERROR_MESSAGE_MS, SUCCESS_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';
import { TeamMembersModalComponent } from '../../widgets/team-members-modal/team-members-modal.component';
import { TeamListComponent } from '../../widgets/team-list/team-list.component';

@Component({
  selector: 'app-team-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TeamMembersModalComponent, TeamListComponent],
  templateUrl: './team-management.component.html',
  styleUrl: './team-management.component.css',
})
export class TeamManagementComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly teamService = inject(TeamService);
  private readonly transientMessages = inject(TransientMessageService);

  saving = false;
  readonly refreshToken = signal(0);
  selectedTeam: PricelistTeam | null = null;
  showMembersModal = false;
  errorMessage = '';
  toastMessage = '';

  readonly form = this.fb.nonNullable.group({
    teamName: ['', [Validators.required, Validators.maxLength(120)]],
  });

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  markRefreshed(): void {
    this.refreshToken.update((v: number) => v + 1);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.transientMessages.clearField(this, 'errorMessage');
    const payload = { teamName: this.form.controls.teamName.value.trim() };

    this.teamService.createTeam(payload).subscribe({
      next: (created) => {
        this.saving = false;
        this.form.reset({ teamName: '' });
        this.showToast('Team created successfully.', SUCCESS_MESSAGE_MS);
        // Trigger list refresh in child component
        this.markRefreshed();
      },
      error: (error) => {
        this.saving = false;
        const message = error?.error?.error ?? 'Failed to create team.';
        if (error?.status === 409) {
          this.transientMessages.setField(this, 'errorMessage', 'A team with that name already exists.', ERROR_MESSAGE_MS);
          return;
        }
        this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS);
      },
    });
  }

  openTeam(team: PricelistTeam): void {
    this.selectedTeam = team;
    this.showMembersModal = true;
  }

  closeMembersModal(): void {
    this.selectedTeam = null;
    this.showMembersModal = false;
  }

  handleTeamUpdated(team: PricelistTeam): void {
    this.selectedTeam = team;
    this.markRefreshed();
  }

  controlError(controlName: 'teamName'): string {
    const control = this.form.controls[controlName];
    if (!control.touched && !control.dirty) {
      return '';
    }
    if (control.hasError('required')) {
      return 'This field is required.';
    }
    if (control.hasError('maxlength')) {
      return 'Team name can be at most 120 characters.';
    }
    return '';
  }

  showToast(message: string, durationMs = ERROR_MESSAGE_MS): void {
    this.transientMessages.setField(this, 'toastMessage', message, durationMs);
  }

  teamMembersText(team: PricelistTeam): string {
    if (team.members.length === 0) {
      return 'No members';
    }

    return team.members.map((member) => member.username).join(', ');
  }
}
