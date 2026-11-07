import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface Customer { id: string; code: string; name: string; phone?: string; }
interface Loan {
  id: string; ticketNo: string; openDate: string; status: string; customerId: string;
  customerName?: string; principalAmount: number; roiPercent: number; fineWeight: number;
  interestAmount: number; totalAmount: number; metalType?: string; itemDesc?: string;
  daysOpen?: number;
}

@Component({
  selector: 'app-loans',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './loans.component.html',
  styleUrl: './loans.component.scss'
})
export class LoansComponent implements OnInit {
  branchId = '';
  customerId = '';
  readonly customers = signal<Customer[]>([]);
  readonly loans = signal<Loan[]>([]);
  readonly message = signal<string | null>(null);
  readonly selected = signal<Customer | null>(null);

  form = {
    principalAmount: 0, roiPercent: 1.5, interestType: 'MONTHLY',
    metalType: 'GOLD', grossWeight: 0, netWeight: 0, fineWeight: 0,
    valuationAmount: 0, itemDesc: '', remarks: ''
  };

  constructor(private readonly http: HttpClient, private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    this.http.get<Array<{ id: string }>>(`${environment.apiBaseUrl}/organization/branches`).subscribe({
      next: (rows) => { if (rows[0]) this.branchId = rows[0].id; }
    });
    this.http.get<Customer[]>(`${environment.apiBaseUrl}/masters/customers`).subscribe({
      next: (rows) => {
        this.customers.set(rows);
        const qid = this.route.snapshot.queryParamMap.get('customerId');
        if (qid) { this.customerId = qid; this.onCustomer(); }
      }
    });
    this.reload();
  }

  onCustomer(): void {
    this.selected.set(this.customers().find((c) => c.id === this.customerId) || null);
    if (this.customerId) {
      this.http.get<Loan[]>(`${environment.apiBaseUrl}/loans/customer/${this.customerId}`).subscribe({
        next: (rows) => this.loans.set(rows),
        error: () => this.reload()
      });
    } else {
      this.reload();
    }
  }

  reload(): void {
    this.http.get<Loan[]>(`${environment.apiBaseUrl}/loans`, { params: { status: 'ACTIVE' } }).subscribe({
      next: (rows) => this.loans.set(rows),
      error: () => this.loans.set([])
    });
  }

  create(): void {
    if (!this.branchId || !this.customerId) {
      this.message.set('Select customer and ensure branch is loaded');
      return;
    }
    this.http.post(`${environment.apiBaseUrl}/loans`, {
      branchId: this.branchId,
      customerId: this.customerId,
      ...this.form
    }).subscribe({
      next: () => {
        this.message.set('Loan created');
        this.form = { ...this.form, principalAmount: 0, fineWeight: 0, itemDesc: '' };
        this.onCustomer();
      },
      error: (e) => this.message.set(e?.error?.message || 'Loan create failed')
    });
  }

  release(id: string): void {
    this.http.post(`${environment.apiBaseUrl}/loans/${id}/release`, {}).subscribe({
      next: () => { this.message.set('Loan released'); this.onCustomer(); },
      error: (e) => this.message.set(e?.error?.message || 'Release failed')
    });
  }

  custName(id: string): string {
    return this.customers().find((c) => c.id === id)?.name || id;
  }
}
