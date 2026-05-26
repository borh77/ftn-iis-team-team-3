import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { UserService } from '../../core/user.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly userService = inject(UserService);
  private readonly cdr = inject(ChangeDetectorRef);

  displayName: string | null = null;

  ngOnInit(): void {
    this.userService.getProfile().subscribe({
      next: (profile) => {
        const firstName = profile.firstName?.trim() ?? '';
        const lastName = profile.lastName?.trim() ?? '';
        const fullName = `${firstName} ${lastName}`.trim();
        this.displayName = fullName || profile.username;
        this.cdr.detectChanges();
      },
      error: () => {
        this.displayName = null;
        this.cdr.detectChanges();
      }
    });
  }

  onLogout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login', { replaceUrl: true });
  }
}
