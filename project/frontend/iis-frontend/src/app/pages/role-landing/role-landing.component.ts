import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-role-landing',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './role-landing.component.html',
  styleUrl: './role-landing.component.css',
})
export class RoleLandingComponent {
  private readonly route = inject(ActivatedRoute);

  get title(): string {
    return this.route.snapshot.data['title'] ?? 'Workspace';
  }

  get subtitle(): string {
    return this.route.snapshot.data['subtitle'] ?? 'Role landing page.';
  }
}
