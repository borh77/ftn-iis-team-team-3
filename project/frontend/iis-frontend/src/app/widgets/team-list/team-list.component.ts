import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, inject } from '@angular/core';
import { TeamService } from '../../core/team.service';
import { PricelistTeam } from '../../core/team.models';

@Component({
  selector: 'app-team-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './team-list.component.html',
  styleUrls: ['./team-list.component.css'],
})
export class TeamListComponent implements OnInit, OnChanges {
  @Input() refreshToken = 0;
  @Output() teamSelected = new EventEmitter<PricelistTeam>();

  private readonly teamService = inject(TeamService);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = false;
  teams: PricelistTeam[] = [];

  ngOnInit(): void {
    this.load();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['refreshToken'] && !changes['refreshToken'].firstChange) {
      this.load();
    }
  }

  load(): void {
    this.loading = true;
    this.teamService.getMyTeams().subscribe({
      next: (teams) => {
        this.teams = [...teams];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.teams = [];
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  open(team: PricelistTeam): void {
    this.teamSelected.emit(team);
  }

  trackById(index: number, item: PricelistTeam): number {
    return item.id;
  }
}
