import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Subscription, debounceTime, distinctUntilChanged } from 'rxjs';
import { PricelistTeam, TeamMember } from '../../core/team.models';
import { TeamService } from '../../core/team.service';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';

@Component({
  selector: 'app-team-members-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './team-members-modal.component.html',
  styleUrl: './team-members-modal.component.css',
})
export class TeamMembersModalComponent implements OnChanges, OnDestroy {
  @Input() team: PricelistTeam | null = null;
  @Output() closed = new EventEmitter<void>();
  @Output() teamChanged = new EventEmitter<PricelistTeam>();

  private readonly teamService = inject(TeamService);
  private readonly transientMessages = inject(TransientMessageService);
  private searchSub: Subscription | null = null;

  currentTeam: PricelistTeam | null = null;
  searchResults: TeamMember[] = [];
  loadingSearch = false;
  submittingMemberId: number | null = null;
  errorMessage = '';

  readonly searchControl = new FormControl('', { nonNullable: true });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['team'] && this.team) {
      this.currentTeam = {
        ...this.team,
        memberIds: [...this.team.memberIds],
        members: [...this.team.members],
      };
      this.clearError();
      this.searchResults = [];
      this.searchControl.setValue('', { emitEvent: false });
      this.bindSearch();
    }
  }

  ngOnDestroy(): void {
    this.searchSub?.unsubscribe();
    this.transientMessages.clearAll(this);
  }

  close(): void {
    this.closed.emit();
  }

  addMember(memberId: number): void {
    if (!this.currentTeam) {
      return;
    }

    this.submittingMemberId = memberId;
    this.clearError();
    this.teamService.addMember(this.currentTeam.id, { memberId }).subscribe({
      next: (team) => {
        this.submittingMemberId = null;
        this.currentTeam = {
          ...team,
          memberIds: [...team.memberIds],
          members: [...team.members],
        };
        this.searchControl.setValue('', { emitEvent: false });
        this.searchResults = [];
        this.teamChanged.emit(this.currentTeam);
      },
      error: (error) => {
        this.submittingMemberId = null;
        this.showError(error?.error?.error ?? 'Failed to add member.');
      },
    });
  }

  removeMember(memberId: number): void {
    if (!this.currentTeam) {
      return;
    }

    this.submittingMemberId = memberId;
    this.clearError();
    this.teamService.removeMember(this.currentTeam.id, { memberId }).subscribe({
      next: (team) => {
        this.submittingMemberId = null;
        this.currentTeam = {
          ...team,
          memberIds: [...team.memberIds],
          members: [...team.members],
        };
        this.teamChanged.emit(this.currentTeam);
      },
      error: (error) => {
        this.submittingMemberId = null;
        this.showError(error?.error?.error ?? 'Failed to remove member.');
      },
    });
  }

  displayName(member: TeamMember): string {
    const firstName = member.firstName?.trim() ?? '';
    const lastName = member.lastName?.trim() ?? '';
    const fullName = `${firstName} ${lastName}`.trim();
    return fullName || member.username;
  }

  isSelected(memberId: number): boolean {
    return this.currentTeam?.memberIds.includes(memberId) ?? false;
  }

  private bindSearch(): void {
    this.searchSub?.unsubscribe();
    this.searchSub = this.searchControl.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged())
      .subscribe((query) => this.performSearch(query));
  }

  private performSearch(query: string): void {
    if (!this.currentTeam) {
      return;
    }

    const normalized = query.trim();
    if (normalized.length < 2) {
      this.searchResults = [];
      this.loadingSearch = false;
      return;
    }

    this.loadingSearch = true;
    this.teamService.searchUsers(normalized).subscribe({
      next: (results) => {
        const currentTeam = this.currentTeam;
        if (!currentTeam) {
          this.searchResults = [];
          this.loadingSearch = false;
          return;
        }

        const blockedIds = new Set([currentTeam.leaderId, ...currentTeam.memberIds]);
        this.searchResults = results.filter((member) => !blockedIds.has(member.id));
        this.loadingSearch = false;
      },
      error: () => {
        this.searchResults = [];
        this.loadingSearch = false;
      },
    });
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS);
  }

  private clearError(): void {
    this.transientMessages.clearField(this, 'errorMessage');
  }
}
