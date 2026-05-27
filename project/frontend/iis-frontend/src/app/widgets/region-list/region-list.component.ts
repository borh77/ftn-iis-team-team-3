import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, inject } from '@angular/core';
import { Region } from '../../core/region.model';
import { RegionService } from '../../core/region.service';

@Component({
  selector: 'app-region-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './region-list.component.html',
  styleUrl: './region-list.component.css',
})
export class RegionListComponent implements OnInit, OnChanges {
  @Input() refreshToken = 0;
  @Output() editRequested = new EventEmitter<Region>();
  @Output() deleteRequested = new EventEmitter<Region>();

  private readonly regionService = inject(RegionService);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = false;
  items: Region[] = [];

  ngOnInit(): void {
    this.reload();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['refreshToken'] && !changes['refreshToken'].firstChange) {
      this.reload();
    }
  }

  reload(): void {
    this.loading = true;
    this.regionService.list().subscribe({
      next: (regions) => {
        this.items = regions;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.items = [];
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }
}