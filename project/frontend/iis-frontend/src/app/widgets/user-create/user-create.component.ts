import { Component, EventEmitter, OnDestroy, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../core/user.service';
import { Region } from '../../core/region.model';
import { RegionService } from '../../core/region.service';
import { UserRole } from '../../core/auth/auth.models';
import { ERROR_MESSAGE_MS, SUCCESS_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';

@Component({
  selector: 'app-user-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-create.component.html',
  styleUrl: './user-create.component.css',
})
export class UserCreateComponent implements OnInit, OnDestroy {
  @Output() created = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly regionService = inject(RegionService);
  private readonly transientMessages = inject(TransientMessageService);

  loading = false;
  loadingRegions = false;
  successMessage = '';
  errorMessage = '';
  regions: Region[] = [];

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    firstName: [''],
    lastName: [''],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['ROLE_ADMIN' as UserRole, Validators.required],
    regionId: [null as number | null],
    customerSegment: [''],
  });

  ngOnInit(): void {
    this.loadRegions();
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  get isBuyerRole(): boolean {
    return this.form.controls.role.value === 'ROLE_BUYER';
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.transientMessages.clearField(this, 'successMessage');
    this.transientMessages.clearField(this, 'errorMessage');

    const raw = this.form.getRawValue();
    const payload = {
      ...raw,
      regionId: raw.role === 'ROLE_BUYER' ? raw.regionId : null,
      customerSegment: raw.role === 'ROLE_BUYER' ? raw.customerSegment.trim() : null,
    };

    this.userService.create(payload).subscribe({
      next: () => {
        this.loading = false;
        this.transientMessages.setField(this, 'successMessage', 'User created successfully.', SUCCESS_MESSAGE_MS);
        this.form.reset({ username: '', email: '', firstName: '', lastName: '', password: '', role: 'ROLE_ADMIN', regionId: null, customerSegment: '' });
        this.created.emit();
      },
      error: (error) => {
        this.loading = false;
        this.transientMessages.setField(this, 'errorMessage', error?.error?.error ?? 'Failed to create user.', ERROR_MESSAGE_MS);
      },
    });
  }

  private loadRegions(): void {
    this.loadingRegions = true;
    this.regionService.list().subscribe({
      next: (regions) => {
        this.regions = regions;
        this.loadingRegions = false;
      },
      error: () => {
        this.regions = [];
        this.loadingRegions = false;
      },
    });
  }
}
