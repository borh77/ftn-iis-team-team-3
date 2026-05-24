import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserCreateComponent } from '../../widgets/user-create/user-create.component';
import { UserListComponent } from '../../widgets/user-list/user-list.component';

@Component({
  selector: 'app-admin-users-page',
  standalone: true,
  imports: [CommonModule, UserCreateComponent, UserListComponent],
  templateUrl: './admin-users-page.component.html',
  styleUrl: './admin-users-page.component.css',
})
export class AdminUsersPageComponent {
  readonly refreshToken = signal(0);

  markRefreshed(): void {
    this.refreshToken.update((value) => value + 1);
  }
}
