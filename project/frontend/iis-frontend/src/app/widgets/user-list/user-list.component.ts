import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChanges, inject } from '@angular/core';
import { UserService } from '../../core/user.service';
import { SpringPage, UserRow } from '../../core/auth/auth.models';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css',
})
export class UserListComponent implements OnChanges {
  @Input() refreshToken = 0;

  private readonly userService = inject(UserService);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = false;
  pageSize = 8;
  page = 0;
  data: SpringPage<UserRow> | null = null;

  ngOnInit(): void {
    this.reload();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['refreshToken'] && !changes['refreshToken'].firstChange) {
      this.reload();
    }
  }

  reload(): void {
    this.page = 0;
    this.load(0);
  }

  load(page = this.page): void {
    this.loading = true;
    this.userService.list(page, this.pageSize).subscribe({
      next: (response) => {
        this.loading = false;
        this.page = response.number;
        this.data = {
          ...response,
          content: [...response.content],
        };
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.data = null;
        this.cdr.detectChanges();
      },
    });
  }

  nextPage(): void {
    if (!this.data || this.data.last) {
      return;
    }

    this.load(this.page + 1);
  }

  previousPage(): void {
    if (this.page === 0) {
      return;
    }

    this.load(this.page - 1);
  }
}
