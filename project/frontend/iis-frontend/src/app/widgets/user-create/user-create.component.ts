import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../core/user.service';

@Component({
  selector: 'app-user-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-create.component.html',
  styleUrl: './user-create.component.css',
})
export class UserCreateComponent {
  @Output() created = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);

  loading = false;
  successMessage = '';
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    firstName: [''],
    lastName: [''],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['ROLE_ADMIN' as const, Validators.required],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.userService.create(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'User created successfully.';
        this.form.reset({ username: '', email: '', firstName: '', lastName: '', password: '', role: 'ROLE_ADMIN' });
        this.created.emit();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error?.error?.error ?? 'Failed to create user.';
      },
    });
  }
}
