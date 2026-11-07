import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.scss'
})
export class OrdersComponent implements OnInit {
  branchId = '';
  readonly customers = signal<any[]>([]);
  readonly metals = signal<any[]>([]);
  readonly orders = signal<any[]>([]);
  readonly message = signal<string | null>(null);

  form = {
    customerId: '',
    itemName: '',
    metalId: '',
    targetWeight: 0,
    estimatedAmount: 0,
    advanceAmount: 0,
    dueDate: '',
    remarks: ''
  };

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any[]>(`${environment.apiBaseUrl}/organization/branches`).subscribe({
      next: (rows) => { if (rows[0]) this.branchId = rows[0].id; }
    });
    this.reload();
  }

  reload(): void {
    this.http.get<any[]>(`${environment.apiBaseUrl}/masters/customers`).subscribe({ next: (r) => this.customers.set(r) });
    this.http.get<any[]>(`${environment.apiBaseUrl}/masters/metals`).subscribe({ next: (r) => this.metals.set(r) });
    this.http.get<any[]>(`${environment.apiBaseUrl}/orders`).subscribe({ next: (r) => this.orders.set(r) });
  }

  save(): void {
    this.http.post<any>(`${environment.apiBaseUrl}/orders`, {
      branchId: this.branchId,
      customerId: this.form.customerId || null,
      itemName: this.form.itemName,
      metalId: this.form.metalId || null,
      targetWeight: this.form.targetWeight || null,
      estimatedAmount: this.form.estimatedAmount || null,
      advanceAmount: this.form.advanceAmount || 0,
      dueDate: this.form.dueDate || null,
      remarks: this.form.remarks
    }).subscribe({
      next: (o) => {
        this.message.set(`Order ${o.orderNo} created`);
        this.form = { customerId: '', itemName: '', metalId: '', targetWeight: 0, estimatedAmount: 0, advanceAmount: 0, dueDate: '', remarks: '' };
        this.reload();
      },
      error: (err) => this.message.set(err?.error?.message || 'Order failed')
    });
  }
}
