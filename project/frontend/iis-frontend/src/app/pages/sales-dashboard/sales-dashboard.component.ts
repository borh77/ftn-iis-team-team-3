import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-sales-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './sales-dashboard.component.html',
  styleUrls: ['./sales-dashboard.component.css'],
})
export class SalesDashboardComponent {
}