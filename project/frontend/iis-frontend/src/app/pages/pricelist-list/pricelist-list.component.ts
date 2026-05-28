import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { PricelistService } from '../../core/pricelist.service';
import { Pricelist } from '../../core/pricelist.models';

@Component({
  selector: 'app-pricelist-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pricelist-list.component.html',
  styleUrls: ['./pricelist-list.component.css'],
})
export class PricelistListComponent implements OnInit {
  private readonly service = inject(PricelistService);

  loading = false;
  pricelists: Pricelist[] = [];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.mine().subscribe({
      next: (list) => {
        console.log('Pricelists.mine response:', list);
        // server returns only current user's pricelists; additionally filter to DRAFT
        this.pricelists = list.filter(p => p.status === 'DRAFT');
      },
      error: () => {
        this.pricelists = [];
      },
      complete: () => (this.loading = false),
    });
  }
}
