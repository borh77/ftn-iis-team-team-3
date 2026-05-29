import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
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
  private readonly cdr = inject(ChangeDetectorRef);

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
        this.loading = false;
        this.pricelists = [...list];
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.pricelists = [];
        this.cdr.detectChanges();
      },
    });
  }

  reload(): void {
    this.load();
  }
}
