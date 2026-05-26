import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = false;
  errorMessage = '';
  lastAuthError = false;

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.form.getRawValue()).subscribe({
      next: (session) => {
        this.loading = false;
        this.lastAuthError = false;
        this.router.navigateByUrl(this.authService.resolveRedirectPath(session));
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.loading = false;
        // Show generic auth message for 401 (unauthorized) and 400 (bad request)
        if (err && err.status && (err.status === 401 || err.status === 400)) {
          this.errorMessage = 'Incorrect username or password.';
          this.lastAuthError = true;
        } else {
          this.errorMessage = 'An unexpected error occurred.';
          this.lastAuthError = false;
        }
        this.cdr.detectChanges();
      },
    });
  }

  onFieldFocus(): void {
    if (this.lastAuthError) {
      this.errorMessage = 'Incorrect username or password.';
      this.cdr.detectChanges();
    }
  }
}
