import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface Customer {
  id: string; code: string; name: string; phone?: string; city?: string;
  firstName?: string; lastName?: string; spouseName?: string; fatherName?: string;
  address?: string; photoBase64?: string;
}

interface Metal { id: string; code: string; name: string; symbol?: string; }
interface Purity { id: string; metalId: string; label: string; fineness: number; }
interface Supplier { id: string; code: string; name: string; phone?: string; city?: string; }

@Component({
  selector: 'app-masters',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './masters.component.html',
  styleUrl: './masters.component.scss'
})
export class MastersComponent implements OnInit {
  tab: 'customers' | 'suppliers' | 'metals' | 'purities' = 'customers';
  readonly customers = signal<Customer[]>([]);
  readonly metals = signal<Metal[]>([]);
  readonly purities = signal<Purity[]>([]);
  readonly suppliers = signal<Supplier[]>([]);
  readonly message = signal<string | null>(null);
  q = '';

  customerForm = {
    title: 'Mr.', firstName: '', lastName: '', spouseName: '', fatherName: '',
    phone: '', altPhone: '', city: '', address: '', email: '', gstin: '', panNo: '', aadhaarNo: ''
  };
  metalForm = { code: '', name: '', symbol: '' };
  purityForm = { metalId: '', label: '', fineness: 91.6 };
  supplierForm = { code: '', name: '', phone: '', city: '' };

  constructor(private readonly http: HttpClient, private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((p) => {
      const qq = p.get('q');
      if (qq) { this.q = qq; this.tab = 'customers'; }
      this.reload();
    });
  }

  reload(): void {
    this.http.get<Customer[]>(`${environment.apiBaseUrl}/masters/customers`, {
      params: this.q.trim() ? { q: this.q.trim() } : {}
    }).subscribe({ next: (r) => this.customers.set(r) });
    this.http.get<Metal[]>(`${environment.apiBaseUrl}/masters/metals`).subscribe({ next: (r) => this.metals.set(r) });
    this.http.get<Purity[]>(`${environment.apiBaseUrl}/masters/purities`).subscribe({ next: (r) => this.purities.set(r) });
    this.http.get<Supplier[]>(`${environment.apiBaseUrl}/masters/suppliers`).subscribe({
      next: (r) => this.suppliers.set(r),
      error: () => this.suppliers.set([])
    });
  }

  saveCustomer(): void {
    this.http.post(`${environment.apiBaseUrl}/masters/customers`, this.customerForm).subscribe({
      next: () => { this.message.set('Customer saved'); this.customerForm = { ...this.customerForm, firstName: '', lastName: '', phone: '' }; this.reload(); },
      error: (e) => this.message.set(e?.error?.message || 'Failed')
    });
  }

  saveMetal(): void {
    this.http.post(`${environment.apiBaseUrl}/masters/metals`, this.metalForm).subscribe({
      next: () => { this.message.set('Metal saved'); this.metalForm = { code: '', name: '', symbol: '' }; this.reload(); },
      error: (e) => this.message.set(e?.error?.message || 'Failed')
    });
  }

  savePurity(): void {
    this.http.post(`${environment.apiBaseUrl}/masters/purities`, this.purityForm).subscribe({
      next: () => { this.message.set('Purity saved'); this.reload(); },
      error: (e) => this.message.set(e?.error?.message || 'Failed')
    });
  }

  saveSupplier(): void {
    this.http.post(`${environment.apiBaseUrl}/masters/suppliers`, this.supplierForm).subscribe({
      next: () => { this.message.set('Supplier saved'); this.reload(); },
      error: (e) => this.message.set(e?.error?.message || 'Failed')
    });
  }

  relationOf(c: Customer): string {
    if (c.spouseName) return `W/o ${c.spouseName}`;
    if (c.fatherName) return `S/o ${c.fatherName}`;
    return '';
  }

  metalName(metalId: string): string {
    return this.metals().find((m) => m.id === metalId)?.name || metalId;
  }

  resolveImageUrl(url: string | undefined): string {
    if (!url) return '';
    if (url.startsWith('data:') || url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }
    const serverOrigin = environment.apiBaseUrl.replace(/\/api\/v1\/?$/, '');
    return url.startsWith('/') ? `${serverOrigin}${url}` : `${serverOrigin}/${url}`;
  }
}
