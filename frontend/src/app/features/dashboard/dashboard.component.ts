import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/auth.service';

interface LedgerRow {
  refNo: string;
  entryDate: string;
  customerName?: string;
  customerPhone?: string;
  amount: number;
}

interface RateTick {
  metalCode: string;
  purityLabel?: string;
  ratePerTenGram: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly message = signal<string | null>(null);
  readonly udhaar = signal<LedgerRow[]>([]);
  readonly advances = signal<LedgerRow[]>([]);
  readonly rates = signal<RateTick[]>([]);
  readonly showQuickAdd = signal(false);
  saving = false;
  chartRange: '7d' | '30d' | '90d' = '30d';

  cust = {
    title: 'Mr.',
    gender: 'M',
    firstName: '',
    lastName: '',
    spouseName: '',
    phone: '',
    altPhone: '',
    address: '',
    countryName: 'India',
    stateName: '',
    city: '',
    village: '',
    pincode: '',
    gstin: '',
    panNo: '',
    aadhaarNo: '',
    email: '',
    partyType: 'CUSTOMER'
  };

  readonly displayName = computed(
    () => this.auth.user()?.displayName || this.auth.user()?.username || 'Admin'
  );

  readonly greeting = computed(() => {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning';
    if (h < 17) return 'Good afternoon';
    return 'Good evening';
  });

  readonly kpis = computed(() => {
    const udhaarTotal = this.udhaar().reduce((s, r) => s + (r.amount || 0), 0);
    const advTotal = this.advances().reduce((s, r) => s + (r.amount || 0), 0);
    const gold = this.rates().find((r) => r.metalCode?.toUpperCase().includes('GOLD'));
    const silver = this.rates().find((r) => r.metalCode?.toUpperCase().includes('SILVER'));
    return [
      {
        key: 'gold',
        label: 'Gold rate / 10g',
        value: gold ? `₹${Number(gold.ratePerTenGram).toLocaleString('en-IN', { maximumFractionDigits: 0 })}` : '—',
        meta: gold?.purityLabel || 'Set in Rates',
        tone: 'gold',
        ico: '◆'
      },
      {
        key: 'silver',
        label: 'Silver rate / 10g',
        value: silver ? `₹${Number(silver.ratePerTenGram).toLocaleString('en-IN', { maximumFractionDigits: 0 })}` : '—',
        meta: silver?.purityLabel || 'Set in Rates',
        tone: 'silver',
        ico: '◇'
      },
      {
        key: 'udhaar',
        label: 'Open udhaar',
        value: `₹${udhaarTotal.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`,
        meta: `${this.udhaar().length} top accounts`,
        tone: 'warn',
        ico: '◎'
      },
      {
        key: 'advance',
        label: 'Customer advances',
        value: `₹${advTotal.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`,
        meta: `${this.advances().length} top accounts`,
        tone: 'ok',
        ico: '▣'
      }
    ];
  });

  readonly statusCards = computed(() => [
    {
      title: 'Collection health',
      value: this.udhaar().length ? 'Attention' : 'Clear',
      detail: this.udhaar().length
        ? `${this.udhaar().length} outstanding parties need follow-up`
        : 'No top udhaar balances flagged',
      tone: this.udhaar().length ? 'warn' : 'ok',
      ico: this.udhaar().length ? '!' : '✓'
    },
    {
      title: 'Advances held',
      value: String(this.advances().length),
      detail: 'Advance balances ready to adjust on next sale',
      tone: 'brand',
      ico: '₹'
    },
    {
      title: 'Rate board',
      value: this.rates().length ? `${this.rates().length} metals` : 'Empty',
      detail: this.rates().length ? 'Live ticker feeding sales & tags' : 'Publish today’s gold / silver rates',
      tone: this.rates().length ? 'ok' : 'muted',
      ico: '▦'
    }
  ]);

  /** UI-only sparkline points — shape scales with selected range */
  readonly trendPoints = computed(() => {
    const seed = this.chartRange === '7d' ? 7 : this.chartRange === '90d' ? 18 : 12;
    const goldBase = Number(this.rates().find((r) => r.metalCode?.toUpperCase().includes('GOLD'))?.ratePerTenGram) || 72000;
    const silverBase = Number(this.rates().find((r) => r.metalCode?.toUpperCase().includes('SILVER'))?.ratePerTenGram) || 92000;
    const gold: number[] = [];
    const silver: number[] = [];
    for (let i = 0; i < seed; i++) {
      const wobble = Math.sin(i / 2.2) * 0.012 + Math.cos(i / 3.1) * 0.008;
      gold.push(Math.round(goldBase * (1 + wobble - 0.01 + i * 0.0015)));
      silver.push(Math.round(silverBase * (1 - wobble * 0.6 + i * 0.001)));
    }
    return { gold, silver };
  });

  readonly goldPath = computed(() => this.toLinePath(this.trendPoints().gold, 560, 180, 8));
  readonly silverPath = computed(() => this.toLinePath(this.trendPoints().silver, 560, 180, 8));
  readonly goldArea = computed(() => this.toAreaPath(this.trendPoints().gold, 560, 180, 8));

  readonly kycSlices = computed(() => {
    const verified = Math.max(this.advances().length, 1);
    const pending = Math.max(this.udhaar().length, 0);
    const rejected = 1;
    const sum = verified + pending + rejected;
    return [
      { label: 'Verified', value: verified, color: '#178a4b', pct: Math.round((verified / sum) * 100) },
      { label: 'Pending', value: pending, color: '#f08a18', pct: Math.round((pending / sum) * 100) },
      { label: 'Review', value: rejected, color: '#9aa1b0', pct: Math.round((rejected / sum) * 100) }
    ];
  });

  readonly donutGradient = computed(() => {
    const slices = this.kycSlices();
    let cursor = 0;
    const parts = slices.map((s) => {
      const start = cursor;
      cursor += s.pct;
      return `${s.color} ${start}% ${cursor}%`;
    });
    return `conic-gradient(${parts.join(', ')})`;
  });

  readonly shortcuts = [
    { label: 'New sale', path: '/app/sales', ico: '◆', tone: 'brand' },
    { label: 'Add stock', path: '/app/inventory', query: { view: 'add' }, ico: '▣', tone: 'ink' },
    { label: 'Customers', path: '/app/masters', ico: '👤', tone: 'ok' },
    { label: 'Loan panel', path: '/app/loans', ico: '◈', tone: 'info' },
    { label: 'Udhaar', path: '/app/credit', ico: '◎', tone: 'warn' },
    { label: 'Rates', path: '/app/rates', ico: '₹', tone: 'gold' }
  ];

  ngOnInit(): void {
    this.reloadLedgers();
    this.reloadRates();
  }

  reloadRates(): void {
    this.http.get<RateTick[]>(`${environment.apiBaseUrl}/rates/ticker`).subscribe({
      next: (rows) => this.rates.set(rows || []),
      error: () => this.rates.set([])
    });
  }

  reloadLedgers(): void {
    this.http.get<any[]>(`${environment.apiBaseUrl}/credit/udhaar/top`, { params: { limit: 5 } }).subscribe({
      next: (rows) => this.udhaar.set(this.mapRows(rows)),
      error: () => this.udhaar.set([])
    });
    this.http.get<any[]>(`${environment.apiBaseUrl}/credit/advance/top`, { params: { limit: 5 } }).subscribe({
      next: (rows) => this.advances.set(this.mapRows(rows)),
      error: () => this.advances.set([])
    });
  }

  mapRows(rows: any[]): LedgerRow[] {
    return (rows || []).map((r) => ({
      refNo: r.customerCode || r.refNo || '—',
      entryDate: r.entryDate || '',
      customerName: r.customerName || r.name,
      customerPhone: r.phone || r.customerPhone || '',
      amount: Number(r.balance ?? r.amount ?? 0)
    }));
  }

  saveCustomer(): void {
    if (!this.cust.firstName?.trim() && !this.cust.phone?.trim()) {
      this.message.set('Enter at least first name or mobile');
      return;
    }
    this.saving = true;
    this.http.post(`${environment.apiBaseUrl}/masters/customers`, this.cust).subscribe({
      next: () => {
        this.message.set('Customer saved');
        this.saving = false;
        this.cust = {
          ...this.cust,
          firstName: '',
          lastName: '',
          phone: '',
          spouseName: '',
          address: '',
          aadhaarNo: '',
          panNo: '',
          gstin: '',
          email: ''
        };
      },
      error: (err) => {
        this.saving = false;
        this.message.set(err?.error?.message || 'Customer save failed');
      }
    });
  }

  go(path: string, query?: Record<string, string>): void {
    void this.router.navigate([path], query ? { queryParams: query } : {});
  }

  setRange(range: '7d' | '30d' | '90d'): void {
    this.chartRange = range;
  }

  private toLinePath(values: number[], width: number, height: number, pad: number): string {
    if (!values.length) return '';
    const min = Math.min(...values);
    const max = Math.max(...values);
    const span = Math.max(max - min, 1);
    return values
      .map((v, i) => {
        const x = pad + (i / Math.max(values.length - 1, 1)) * (width - pad * 2);
        const y = height - pad - ((v - min) / span) * (height - pad * 2);
        return `${i === 0 ? 'M' : 'L'}${x.toFixed(1)},${y.toFixed(1)}`;
      })
      .join(' ');
  }

  private toAreaPath(values: number[], width: number, height: number, pad: number): string {
    const line = this.toLinePath(values, width, height, pad);
    if (!line) return '';
    const lastX = pad + ((values.length - 1) / Math.max(values.length - 1, 1)) * (width - pad * 2);
    return `${line} L${lastX.toFixed(1)},${height - pad} L${pad},${height - pad} Z`;
  }
}
