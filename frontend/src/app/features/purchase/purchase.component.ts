import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-purchase',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './purchase.component.html',
  styleUrl: './purchase.component.scss'
})
export class PurchaseComponent implements OnInit {
  branchId = '';
  supplierId = '';
  readonly suppliers = signal<any[]>([]);
  readonly items = signal<any[]>([]);
  readonly metals = signal<any[]>([]);
  readonly purities = signal<any[]>([]);
  readonly invoices = signal<any[]>([]);
  readonly message = signal<string | null>(null);

  newSupplier = { name: '', phone: '', city: '', gstin: '' };
  line = {
    stockItemId: '', itemName: '', metalId: '', purityId: '', quantity: 1,
    grossWeight: 0, stoneWeight: 0, purityPercent: 91.6, wastagePercent: 0,
    ratePerGram: 0, makingType: 'PER_GRAM', makingRate: 0, stoneAmount: 0
  };

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any[]>(`${environment.apiBaseUrl}/organization/branches`).subscribe({
      next: (rows) => { if (rows[0]) this.branchId = rows[0].id; }
    });
    this.reload();
  }

  reload(): void {
    this.http.get<any[]>(`${environment.apiBaseUrl}/masters/suppliers`).subscribe({ next: (r) => this.suppliers.set(r) });
    this.http.get<any[]>(`${environment.apiBaseUrl}/masters/metals`).subscribe({ next: (r) => this.metals.set(r) });
    this.http.get<any[]>(`${environment.apiBaseUrl}/masters/purities`).subscribe({ next: (r) => this.purities.set(r) });
    this.http.get<any[]>(`${environment.apiBaseUrl}/inventory/items`).subscribe({ next: (r) => this.items.set(r) });
    this.http.get<any[]>(`${environment.apiBaseUrl}/purchase/invoices`).subscribe({ next: (r) => this.invoices.set(r) });
  }

  saveSupplier(): void {
    this.http.post<any>(`${environment.apiBaseUrl}/masters/suppliers`, this.newSupplier).subscribe({
      next: (s) => {
        this.supplierId = s.id;
        this.newSupplier = { name: '', phone: '', city: '', gstin: '' };
        this.reload();
        this.message.set(`Supplier ${s.name} saved`);
      },
      error: (err) => this.message.set(err?.error?.message || 'Supplier failed')
    });
  }

  onStockPick(): void {
    const item = this.items().find((i) => i.id === this.line.stockItemId);
    if (!item) return;
    this.line.itemName = item.name;
    this.line.metalId = item.metalId || '';
    this.line.purityId = item.purityId || '';
  }

  filteredPurities() {
    return this.purities().filter((p) => !this.line.metalId || p.metalId === this.line.metalId);
  }

  savePurchase(): void {
    this.http.post<any>(`${environment.apiBaseUrl}/purchase/invoices`, {
      branchId: this.branchId,
      supplierId: this.supplierId || null,
      lines: [{ ...this.line, stockItemId: this.line.stockItemId || null }]
    }).subscribe({
      next: (inv) => {
        this.message.set(`Purchase ${inv.invoiceNo} · ₹${inv.grandTotal}`);
        this.reload();
      },
      error: (err) => this.message.set(err?.error?.message || 'Purchase failed')
    });
  }
}
