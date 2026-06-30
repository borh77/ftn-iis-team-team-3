import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { UserService } from '../../core/user.service';
import { ERROR_MESSAGE_MS, SUCCESS_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';

@Component({
  selector: 'app-force-password-change',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './force-password-change.component.html',
  styleUrl: './force-password-change.component.css',
})
export class ForcePasswordChangeComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  loading = false;
  errorMessage = '';
  successMessage = '';

  readonly form = this.fb.nonNullable.group(
    {
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[0-9])(?=.*[!@#$%^&*]).+$/)]],
      confirmNewPassword: ['', Validators.required],
    },
    { validators: [this.passwordsMatchValidator, this.passwordsDifferValidator] },
  );

  get username(): string {
    return this.authService.currentSession?.username ?? 'User';
  }

  ngOnInit(): void {
    if (!this.authService.requiresPasswordChange()) {
      void this.router.navigateByUrl(this.authService.resolveRedirectPath(this.authService.currentSession));
    }
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.clearResultMessages();

    this.userService.changePassword(this.form.getRawValue()).subscribe({
      next: (session) => {
        this.loading = false;
        this.authService.applySessionResponse(session);
        this.showSuccess('Password changed successfully.');
        this.cdr.detectChanges();
        void this.router.navigateByUrl(this.authService.resolveRedirectPath(session));
      },
      error: (error) => {
        this.loading = false;
        this.showError(error?.error?.error ?? 'Unable to change password.');
        this.cdr.detectChanges();
      },
    });
  }

  private passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
    const newPassword = control.get('newPassword')?.value as string;
    const confirmNewPassword = control.get('confirmNewPassword')?.value as string;

    return newPassword && confirmNewPassword && newPassword !== confirmNewPassword
      ? { passwordsMismatch: true }
      : null;
  }

  private passwordsDifferValidator(control: AbstractControl): ValidationErrors | null {
    const oldPassword = control.get('oldPassword')?.value as string;
    const newPassword = control.get('newPassword')?.value as string;

    return oldPassword && newPassword && oldPassword === newPassword
      ? { passwordUnchanged: true }
      : null;
  }

  private showSuccess(message: string): void {
    this.transientMessages.setField(this, 'successMessage', message, SUCCESS_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private clearResultMessages(): void {
    this.transientMessages.clearField(this, 'successMessage', () => this.cdr.detectChanges());
    this.transientMessages.clearField(this, 'errorMessage', () => this.cdr.detectChanges());
  }
}
