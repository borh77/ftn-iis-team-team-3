import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LogOut, LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { UserService } from '../../core/user.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {
  readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly userService = inject(UserService);
  private readonly cdr = inject(ChangeDetectorRef);
  private profileSub: Subscription | null = null;

  displayName: string | null = null;
  readonly icons = { LogOut };

  ngOnInit(): void {
    this.profileSub = this.userService.profile$.subscribe((profile) => {
      if (profile) {
        const firstName = profile.firstName?.trim() ?? '';
        const lastName = profile.lastName?.trim() ?? '';
        const fullName = `${firstName} ${lastName}`.trim();
        this.displayName = fullName || profile.username;
      } else {
        this.displayName = null;
      }
      this.cdr.detectChanges();
    });

    this.userService.getProfile().subscribe({ error: () => undefined });
  }

  ngOnDestroy(): void {
    this.profileSub?.unsubscribe();
  }

  onLogout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login', { replaceUrl: true });
  }
}
