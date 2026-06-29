import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ReactiveFormsModule, UntypedFormGroup } from '@angular/forms';
import { PricelistTeam } from '../../core/team.models';

@Component({
  selector: 'app-pricelist-wizard-team-access-step',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <form class="wizard-form" [formGroup]="form">
      <div class="choice-stack">
        <label class="choice-row">
          <input type="radio" formControlName="accessMode" value="PRIVATE" />
          <span>
            <strong>Private pricelist</strong>
            <small>Only you can continue and manage this draft.</small>
          </span>
        </label>

        <label class="choice-row">
          <input type="radio" formControlName="accessMode" value="TEAM" />
          <span>
            <strong>Team pricelist</strong>
            <small>Select one of your available teams for collaboration.</small>
          </span>
        </label>
      </div>

      @if (form.controls['accessMode'].value === 'TEAM') {
        <label class="wide-field">
          <span>Team</span>
          <select formControlName="teamId">
            <option [ngValue]="null">Select team</option>
            @for (team of teams; track team.id) {
              <option [ngValue]="team.id">{{ team.name }}</option>
            }
          </select>
          @if (form.get('teamId')?.touched && form.hasError('teamRequired')) {
            <small class="field-error">Select a team or choose private pricelist.</small>
          }
        </label>

        @if (!teams.length) {
          <p class="muted">No available teams were found.</p>
        }
      }
    </form>
  `,
})
export class PricelistWizardTeamAccessStepComponent {
  @Input({ required: true }) form!: UntypedFormGroup;
  @Input() teams: PricelistTeam[] = [];
}
