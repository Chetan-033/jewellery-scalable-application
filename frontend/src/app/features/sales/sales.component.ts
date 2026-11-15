import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiPage, pageContent } from '../../core/api-page';

interface Customer {
  id: string; code: string; name: string; phone?: string; city?: string;
  firstName?: string; lastName?: string; address?: string; aadhaarNo?: string;
}
interface Metal { id: string; code: string; name: string; }
interface Purity { id: string; metalId: string; label: string; fineness: number; }
interface LookupItem {
  id: string; sku: string; name: string; metalId?: string; purityId?: string;
  barcode?: string; purityPercent?: number;
}

interface SaleLine {
  stockItemId: string;
  barcode: string;
  itemName: string;
  metalId: string;
  purityId: string;
  quantity: number;
  grossWeight: number;
  stoneWeight: number;
  purityPercent: number;
  wastagePercent: number;
  custWastagePercent: number;
  ratePerGram: number;
  makingType: string;
  makingRate: number;
  stoneAmount: number;
  valueAdded: number;
  discountAmount: number;
  previewTotal: number | null;
  previewFine: number | null;
  previewFinalFine: number | null;
}

interface ExchangeLine {
  metalType: string;
  description: string;
  grossWeight: number;
  lessWeight: number;
  purityPercent: number;
  ratePerGram: number;
}

@Component({
  selector: 'app-sales',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sales.component.html',
  styleUrl: './sales.component.scss'
})
export class SalesComponent implements OnInit {
  branchId = '';
  customerId = '';
  customerQuery = '';
  addQuery = '';
  showCustomerPanel = false;
  sellTab: 'BILL' | 'LIST' = 'BILL';
  readonly customers = signal<Customer[]>([]);
  readonly metals = signal<Metal[]>([]);
  readonly purities = signal<Purity[]>([]);
  readonly lookups = signal<LookupItem[]>([]);
  readonly invoices = signal<any[]>([]);
  readonly message = signal<string | null>(null);
  readonly selectedCustomer = signal<Customer | null>(null);

  newCustomer = {
    title: 'Ms.', firstName: '', lastName: '', spouseName: '', phone: '', altPhone: '',
    city: '', address: '', fatherName: '', aadhaarNo: '', panNo: '', gstin: '', email: '', gender: 'F'
  };

  lines: SaleLine[] = [];
  exchange: ExchangeLine = {
    metalType: 'GOLD', description: '', grossWeight: 0, lessWeight: 0, purityPercent: 91.6, ratePerGram: 0
  };

  pay = {
    cashAmount: 0, cardAmount: 0, onlineAmount: 0, chequeAmount: 0, udhaarAmount: 0,
    hallmarkAmount: 0, otherCharges: 0, courierCharges: 0,
    cgstPercent: 1.5, sgstPercent: 1.5, applyCgst: true, applySgst: true
  };

  constructor(private readonly http: HttpClient, private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    this.http.get<Array<{ id: string }>>(`${environment.apiBaseUrl}/organization/branches`).subscribe({
      next: (rows) => { if (rows[0]) this.branchId = rows[0].id; }
    });
    this.reloadMasters();
    this.reloadInvoices();
    const presetCustomer = this.route.snapshot.queryParamMap.get('customerId');
    if (presetCustomer) {
      this.customerId = presetCustomer;
    }
  }

  emptyLine(): SaleLine {
    return {
      stockItemId: '', barcode: '', itemName: '', metalId: '', purityId: '', quantity: 1,
      grossWeight: 0, stoneWeight: 0, purityPercent: 91.6, wastagePercent: 0, custWastagePercent: 0,
      ratePerGram: 0, makingType: 'PP', makingRate: 0, stoneAmount: 0, valueAdded: 0,
      discountAmount: 0, previewTotal: null, previewFine: null, previewFinalFine: null
    };
  }

  readonly rateMap = signal<Record<string, number>>({});

  reloadMasters(): void {
    this.http.get<Metal[]>(`${environment.apiBaseUrl}/masters/metals`).subscribe({ next: (r) => this.metals.set(r) });
    this.http.get<Purity[]>(`${environment.apiBaseUrl}/masters/purities`).subscribe({ next: (r) => this.purities.set(r) });
    this.http.get<Array<{ metalCode: string; purityLabel?: string; ratePerGram: number }>>(
      `${environment.apiBaseUrl}/rates/ticker`
    ).subscribe({
      next: (rows) => {
        const map: Record<string, number> = {};
        for (const r of rows || []) {
          map[`${r.metalCode}|${r.purityLabel || ''}`] = Number(r.ratePerGram);
          if (!map[r.metalCode]) map[r.metalCode] = Number(r.ratePerGram);
        }
        this.rateMap.set(map);
      }
    });
    this.searchCustomers();
  }

  applyRate(line: SaleLine): void {
    const metal = this.metals().find((m) => m.id === line.metalId);
    const purity = this.purities().find((p) => p.id === line.purityId);
    if (!metal) return;
    const key = `${metal.code}|${purity?.label || ''}`;
    const rate = this.rateMap()[key] ?? this.rateMap()[metal.code];
    if (rate != null && (!line.ratePerGram || line.ratePerGram === 0)) {
      line.ratePerGram = rate;
    }
  }

  reloadInvoices(): void {
    this.http.get<ApiPage<any>>(`${environment.apiBaseUrl}/sales/invoices`, {
      params: { page: 0, size: 50 }
    }).subscribe({
      next: (page) => this.invoices.set(pageContent(page)),
      error: () => this.invoices.set([])
    });
  }

  searchCustomers(): void {
    const q = this.customerQuery?.trim() || '';
    const params: Record<string, string | number> = { page: 0, size: 50 };
    if (q) params['q'] = q;
    this.http.get<ApiPage<Customer>>(`${environment.apiBaseUrl}/masters/customers`, { params }).subscribe({
      next: (page) => {
        this.customers.set(pageContent(page));
        if (this.customerId) {
          this.pickCustomer();
        }
      },
      error: () => this.customers.set([])
    });
  }

  pickCustomer(): void {
    const c = this.customers().find((x) => x.id === this.customerId) || null;
    this.selectedCustomer.set(c);
  }

  saveCustomer(): void {
    this.http.post<Customer>(`${environment.apiBaseUrl}/masters/customers`, this.newCustomer).subscribe({
      next: (c) => {
        this.message.set(`Customer ${c.code} saved`);
        this.customerId = c.id;
        this.showCustomerPanel = false;
        this.reloadMasters();
        this.selectedCustomer.set(c);
      },
      error: (err) => this.message.set(err?.error?.message || 'Customer save failed')
    });
  }

  searchAdd(): void {
    const q = this.addQuery.trim();
    if (!q) { this.lookups.set([]); return; }
    this.http.get<LookupItem[]>(`${environment.apiBaseUrl}/inventory/items/lookup`, { params: { q } }).subscribe({
      next: (rows) => this.lookups.set(rows),
      error: () => this.lookups.set([])
    });
  }

  addLookup(item: LookupItem): void {
    const line = this.emptyLine();
    line.stockItemId = item.id;
    line.itemName = item.name;
    line.barcode = item.barcode || item.sku;
    line.metalId = item.metalId || '';
    line.purityId = item.purityId || '';
    if (item.purityPercent != null) line.purityPercent = Number(item.purityPercent);
    else {
      const purity = this.purities().find((p) => p.id === line.purityId);
      if (purity) line.purityPercent = Number(purity.fineness);
    }
    this.applyRate(line);
    this.lines = [...this.lines, line];
    this.addQuery = '';
    this.lookups.set([]);
    this.previewLine(this.lines.length - 1);
  }

  addManualLine(): void {
    this.lines = [...this.lines, this.emptyLine()];
  }

  removeLine(i: number): void {
    this.lines = this.lines.filter((_, idx) => idx !== i);
  }

  filteredPurities(metalId: string): Purity[] {
    return this.purities().filter((p) => !metalId || p.metalId === metalId);
  }

  onLinePurity(i: number): void {
    const line = this.lines[i];
    if (!line) return;
    const purity = this.purities().find((p) => p.id === line.purityId);
    if (purity) {
      line.purityPercent = Number(purity.fineness);
      line.metalId = purity.metalId;
    }
    this.applyRate(line);
    this.previewLine(i);
  }

  previewLine(i: number): void {
    const line = this.lines[i];
    if (!line || !line.grossWeight || !line.purityPercent) return;
    this.http.post<any>(`${environment.apiBaseUrl}/calc/metal-line`, {
      quantity: line.quantity,
      grossWeight: line.grossWeight,
      stoneWeight: line.stoneWeight,
      purityPercent: line.purityPercent,
      wastagePercent: line.wastagePercent,
      custWastagePercent: line.custWastagePercent,
      ratePerGram: line.ratePerGram,
      makingType: line.makingType,
      makingRate: line.makingRate,
      stoneAmount: line.stoneAmount,
      valueAdded: line.valueAdded,
      discountAmount: line.discountAmount,
      cgstPercent: this.pay.applyCgst ? this.pay.cgstPercent : 0,
      sgstPercent: this.pay.applySgst ? this.pay.sgstPercent : 0,
      igstPercent: 0
    }).subscribe({
      next: (res) => {
        line.previewTotal = Number(res.lineTotal);
        line.previewFine = Number(res.fineWeight);
        line.previewFinalFine = Number(res.finalFineWeight);
      }
    });
  }

  previewAll(): void {
    this.lines.forEach((_, i) => this.previewLine(i));
  }

  linesSubtotal(): number {
    return this.lines.reduce((s, l) => s + (l.previewTotal || 0), 0);
  }

  exchangeVal(): number {
    const net = Math.max(0, (this.exchange.grossWeight || 0) - (this.exchange.lessWeight || 0));
    const fine = net * ((this.exchange.purityPercent || 0) / 100);
    return fine * (this.exchange.ratePerGram || 0);
  }

  taxOn(base: number, pct: number): number {
    return Math.round(base * pct) / 100;
  }

  taxableBase(): number {
    return Math.max(0, this.linesSubtotal() + (this.pay.hallmarkAmount || 0) + (this.pay.otherCharges || 0) + (this.pay.courierCharges || 0));
  }

  cgstAmt(): number { return this.pay.applyCgst ? this.taxOn(this.taxableBase(), this.pay.cgstPercent) : 0; }
  sgstAmt(): number { return this.pay.applySgst ? this.taxOn(this.taxableBase(), this.pay.sgstPercent) : 0; }

  grandTotal(): number {
    return Math.max(0, this.taxableBase() + this.cgstAmt() + this.sgstAmt() - this.exchangeVal());
  }

  received(): number {
    return (this.pay.cashAmount || 0) + (this.pay.cardAmount || 0) + (this.pay.onlineAmount || 0)
      + (this.pay.chequeAmount || 0) + (this.pay.udhaarAmount || 0);
  }

  balanceDue(): number {
    return Math.round((this.grandTotal() - this.received()) * 100) / 100;
  }

  fillCash(): void {
    this.pay.cashAmount = this.grandTotal();
    this.pay.cardAmount = 0;
    this.pay.onlineAmount = 0;
    this.pay.chequeAmount = 0;
    this.pay.udhaarAmount = 0;
  }

  finalizeSale(): void {
    if (!this.branchId) { this.message.set('Branch missing'); return; }
    if (!this.lines.length) { this.message.set('Add at least one item'); return; }
    if (this.pay.udhaarAmount > 0 && !this.customerId) {
      this.message.set('Select customer for udhaar');
      return;
    }
    this.previewAll();
    const exchangeLines = (this.exchange.grossWeight > 0)
      ? [{
          metalType: this.exchange.metalType,
          description: this.exchange.description,
          grossWeight: this.exchange.grossWeight,
          lessWeight: this.exchange.lessWeight,
          purityPercent: this.exchange.purityPercent,
          ratePerGram: this.exchange.ratePerGram
        }]
      : [];

    const body = {
      branchId: this.branchId,
      customerId: this.customerId || null,
      cashAmount: this.pay.cashAmount,
      cardAmount: this.pay.cardAmount,
      onlineAmount: this.pay.onlineAmount,
      chequeAmount: this.pay.chequeAmount,
      udhaarAmount: this.pay.udhaarAmount,
      hallmarkAmount: this.pay.hallmarkAmount,
      otherCharges: this.pay.otherCharges,
      courierCharges: this.pay.courierCharges,
      exchangeLines,
      lines: this.lines.map((l) => ({
        stockItemId: l.stockItemId || null,
        itemName: l.itemName,
        metalId: l.metalId || null,
        purityId: l.purityId || null,
        quantity: l.quantity,
        grossWeight: l.grossWeight,
        stoneWeight: l.stoneWeight,
        purityPercent: l.purityPercent,
        wastagePercent: l.wastagePercent,
        custWastagePercent: l.custWastagePercent,
        ratePerGram: l.ratePerGram,
        makingType: l.makingType,
        makingRate: l.makingRate,
        stoneAmount: l.stoneAmount,
        valueAdded: l.valueAdded,
        discountAmount: l.discountAmount,
        barcode: l.barcode,
        cgstPercent: this.pay.applyCgst ? this.pay.cgstPercent : 0,
        sgstPercent: this.pay.applySgst ? this.pay.sgstPercent : 0
      }))
    };

    this.http.post<any>(`${environment.apiBaseUrl}/sales/invoices`, body).subscribe({
      next: (res) => {
        this.message.set(`Sale ${res.invoiceNo} finalized · ₹ ${res.grandTotal}`);
        this.lines = [];
        this.exchange = { metalType: 'GOLD', description: '', grossWeight: 0, lessWeight: 0, purityPercent: 91.6, ratePerGram: 0 };
        this.pay = { ...this.pay, cashAmount: 0, cardAmount: 0, onlineAmount: 0, chequeAmount: 0, udhaarAmount: 0 };
        this.reloadInvoices();
      },
      error: (err) => this.message.set(err?.error?.message || 'Sale failed')
    });
  }
}
