import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface Customer { id: string; code: string; name: string; phone?: string; }
interface Entry {
  id?: string; refNo?: string; entryDate?: string; entryType?: string;
  amount?: number; balanceAfter?: number; balance?: number;
  customerName?: string; customerPhone?: string; phone?: string; customerCode?: string; remarks?: string;
}

@Component({
  selector: 'app-credit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './credit.component.html',
  styleUrl: './credit.component.scss'
})
export class CreditComponent implements OnInit {
  branchId = '';
  customerId = '';
  mode: 'UDHAAR' | 'ADVANCE' = 'UDHAAR';
  amount = 0;
  remarks = '';
  readonly customers = signal<Customer[]>([]);
  readonly udhaar = signal<Entry[]>([]);
  readonly advances = signal<Entry[]>([]);
  readonly ledger = signal<Entry[]>([]);
  readonly message = signal<string | null>(null);

  constructor(private readonly http: HttpClient, private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    this.http.get<Array<{ id: string }>>(`${environment.apiBaseUrl}/organization/branches`).subscribe({
      next: (rows) => { if (rows[0]) this.branchId = rows[0].id; }
    });
    this.http.get<Customer[]>(`${environment.apiBaseUrl}/masters/customers`).subscribe({
      next: (rows) => {
        this.customers.set(rows);
        const qid = this.route.snapshot.queryParamMap.get('customerId');
        if (qid) { this.customerId = qid; this.loadCustomerLedger(); }
      }
    });
    this.reloadTops();
  }

  reloadTops(): void {
    this.http.get<Entry[]>(`${environment.apiBaseUrl}/credit/udhaar/top`, { params: { limit: 10 } }).subscribe({
      next: (r) => this.udhaar.set(r), error: () => this.udhaar.set([])
    });
    this.http.get<Entry[]>(`${environment.apiBaseUrl}/credit/advance/top`, { params: { limit: 10 } }).subscribe({
      next: (r) => this.advances.set(r), error: () => this.advances.set([])
    });
  }

  loadCustomerLedger(): void {
    if (!this.customerId) { this.ledger.set([]); return; }
    this.http.get<any>(`${environment.apiBaseUrl}/credit/customer/${this.customerId}`).subscribe({
      next: (res) => this.ledger.set(res.entries || res || []),
      error: () => this.ledger.set([])
    });
  }

  post(): void {
    if (!this.branchId || !this.customerId || !this.amount) {
      this.message.set('Customer, branch and amount required');
      return;
    }
    const path = this.mode === 'UDHAAR' ? 'udhaar' : 'advance';
    this.http.post(`${environment.apiBaseUrl}/credit/${path}`, {
      branchId: this.branchId,
      customerId: this.customerId,
      amount: this.amount,
      remarks: this.remarks
    }).subscribe({
      next: () => {
        this.message.set(`${this.mode} posted`);
        this.amount = 0;
        this.remarks = '';
        this.reloadTops();
        this.loadCustomerLedger();
      },
      error: (e) => this.message.set(e?.error?.message || 'Post failed')
    });
  }
}
