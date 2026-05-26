import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { UserService } from '../../core/user.service';
import { UserRow } from '../../core/auth/auth.models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css',
})
export class ProfileComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);

  profile: UserRow | null = null;
  loadingProfile = false;
  profileMessage = '';
  passwordMessage = '';
  savingProfile = false;
  changingPassword = false;

  readonly profileForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(100)]],
    firstName: ['', [Validators.maxLength(100)]],
    lastName: ['', [Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
  });

  readonly passwordForm = this.fb.nonNullable.group(
    {
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[0-9])(?=.*[!@#$%^&*]).+$/)]],
      confirmNewPassword: ['', Validators.required],
    },
    { validators: [this.passwordsMatchValidator, this.passwordsDifferValidator] },
  );

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loadingProfile = true;
    this.userService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileForm.patchValue({
          username: profile.username,
          firstName: profile.firstName ?? '',
          lastName: profile.lastName ?? '',
          email: profile.email,
        });
        this.loadingProfile = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingProfile = false;
        this.profileMessage = 'Unable to load profile data.';
        this.cdr.detectChanges();
      },
    });
  }

  submitProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.savingProfile = true;
    this.profileMessage = '';

    this.userService.updateProfile(this.profileForm.getRawValue()).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.authService.applySessionResponse(profile);
        this.savingProfile = false;
        this.profileMessage = 'Profile updated successfully.';
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.savingProfile = false;
        this.profileMessage = error?.error?.error ?? 'Unable to update profile.';
        this.cdr.detectChanges();
      },
    });
  }

  submitPassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.changingPassword = true;
    this.passwordMessage = '';

    this.userService.changePassword(this.passwordForm.getRawValue()).subscribe({
      next: (session) => {
        this.changingPassword = false;
        this.authService.applySessionResponse(session);
        this.passwordMessage = 'Password changed successfully.';
        this.passwordForm.reset();
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.changingPassword = false;
        this.passwordMessage = error?.error?.error ?? 'Unable to change password.';
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
}