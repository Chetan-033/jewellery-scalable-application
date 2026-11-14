import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiPage, pageContent } from '../../core/api-page';

interface Metal { id: string; code: string; name: string; }
interface Purity { id: string; metalId: string; label: string; fineness: number; }
interface StockItem {
  id: string; sku: string; name: string; metalId?: string; purityId?: string;
  category?: string; stockType?: string; barcode?: string; rfidNo?: string; status: string;
}
interface StockSettings {
  stockValBy: string; stockMkgBy: string; stockCsWstgBy: string;
  stockLbrBy: string; addStockFinalFinewt: string; stockFineBaseBy: string;
}
interface PacketRow { description: string; quantity: number; weight: number; operation: string; }
interface CrystalRow {
  category: string; name: string; clarity: string; color: string; shape: string;
  sizeLabel: string; quantity: number; weight: number; rate: number; rateType: string; autoLess: boolean;
}
interface Preview {
  netWeight: number; fineWeight: number; wastageWeight: number; finalFineWeight: number;
  finalPurityPercent: number; metalValuation: number; totalLabCharges: number;
  totalMakingCharges: number; totalOtherCharges: number; totalHallmarkCharges: number;
  stoneValuation: number; taxAmount: number; finalValuation: number;
  valuationBy: string; formulaNotes: string;
}
interface StockEntry {
  id: string; entryNo: string; itemName: string; stockType: string; quantity: number;
  grossWeight: number; netWeight: number; finalFineWeight: number; finalValuation: number;
  barcode?: string; status: string; itemCategory?: string; metalId?: string;
}
interface Summary {
  goldGross: number; goldNet: number; silverGross: number; silverNet: number;
  itemCount: number; retailCount: number;
}

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss'
})
export class InventoryComponent implements OnInit {
  view: 'panel' | 'add' = 'panel';
  stockTab: 'FINE' | 'RAW' = 'FINE';
  readonly metals = signal<Metal[]>([]);
  readonly purities = signal<Purity[]>([]);
  readonly items = signal<StockItem[]>([]);
  readonly entries = signal<StockEntry[]>([]);
  readonly settings = signal<StockSettings | null>(null);
  readonly preview = signal<Preview | null>(null);
  readonly summary = signal<Summary | null>(null);
  readonly message = signal<string | null>(null);
  readonly saving = signal(false);
  showSettings = false;
  branchId = '';
  filterMetal = '';
  filterCategory = '';
  filterName = '';

  form = this.emptyForm();
  packets: PacketRow[] = [this.emptyPacket()];
  crystals: CrystalRow[] = [];

  constructor(private readonly http: HttpClient, private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((p) => {
      const v = p.get('view');
      this.view = v === 'add' ? 'add' : 'panel';
    });
    this.http.get<Array<{ id: string }>>(`${environment.apiBaseUrl}/organization/branches`).subscribe({
      next: (rows) => { if (rows[0]) this.branchId = rows[0].id; }
    });
    this.reload();
  }

  emptyForm() {
    return {
      stockItemId: '', sku: '', itemName: '', itemCategory: '', stockType: 'RETAIL',
      metalId: '', purityId: '', barcodePrefix: '', barcode: '', rfidNo: '', hsnNo: '',
      itemSize: '', otherInfo: '', quantity: 1, grossWeight: 0, pktWeight: 0, lessWeight: 0,
      tagWeight: 0, purityPercent: 91.6, wastagePercent: 0, metalRate: 0,
      labCharges: 0, labChargesType: 'GM', makingCharges: 0, makingChargesType: 'GM',
      otherCharges: 0, otherChargesType: 'FX', hallmarkCharges: 0, hallmarkChargesType: 'FX',
      taxPercent: 0, brandName: '', counterName: ''
    };
  }
  emptyPacket(): PacketRow { return { description: '', quantity: 1, weight: 0, operation: 'LESS' }; }
  emptyCrystal(): CrystalRow {
    return { category: '', name: '', clarity: '', color: '', shape: '', sizeLabel: '',
      quantity: 1, weight: 0, rate: 0, rateType: 'PP', autoLess: true };
  }

  reload(): void {
    this.http.get<Metal[]>(`${environment.apiBaseUrl}/masters/metals`).subscribe({ next: (r) => this.metals.set(r) });
    this.http.get<Purity[]>(`${environment.apiBaseUrl}/masters/purities`).subscribe({ next: (r) => this.purities.set(r) });
    this.http.get<ApiPage<StockItem>>(`${environment.apiBaseUrl}/inventory/items`, {
      params: { page: 0, size: 100 }
    }).subscribe({ next: (r) => this.items.set(pageContent(r)), error: () => this.items.set([]) });
    this.http.get<ApiPage<StockEntry>>(`${environment.apiBaseUrl}/inventory/add-stock`, {
      params: { page: 0, size: 50 }
    }).subscribe({ next: (r) => this.entries.set(pageContent(r)), error: () => this.entries.set([]) });
    this.http.get<StockSettings>(`${environment.apiBaseUrl}/calc/settings/stock-add`).subscribe({ next: (r) => this.settings.set(r) });
    this.http.get<Summary>(`${environment.apiBaseUrl}/inventory/summary`).subscribe({
      next: (r) => this.summary.set(r),
      error: () => this.summary.set(null)
    });
  }

  filteredPurities(): Purity[] {
    return this.purities().filter((p) => !this.form.metalId || p.metalId === this.form.metalId);
  }

  filteredEntries(): StockEntry[] {
    return this.entries().filter((e) => {
      if (this.filterCategory && !(e.itemCategory || '').toLowerCase().includes(this.filterCategory.toLowerCase())) return false;
      if (this.filterName && !e.itemName.toLowerCase().includes(this.filterName.toLowerCase())) return false;
      if (this.stockTab === 'FINE' && e.stockType === 'RAW') return false;
      if (this.stockTab === 'RAW' && e.stockType !== 'RAW') return false;
      if (this.filterMetal && e.metalId !== this.filterMetal) return false;
      return true;
    });
  }

  onExistingPick(): void {
    const item = this.items().find((i) => i.id === this.form.stockItemId);
    if (!item) return;
    this.form.sku = item.sku;
    this.form.itemName = item.name;
    this.form.metalId = item.metalId || '';
    this.form.purityId = item.purityId || '';
    this.form.itemCategory = item.category || '';
    this.form.stockType = item.stockType || 'RETAIL';
    this.form.barcode = item.barcode || '';
    this.form.rfidNo = item.rfidNo || '';
    this.onPurityPick();
  }

  onPurityPick(): void {
    const purity = this.purities().find((p) => p.id === this.form.purityId);
    if (purity) {
      this.form.purityPercent = Number(purity.fineness);
      this.form.metalId = purity.metalId;
    }
    this.applySeedRate();
    this.runPreview();
  }

  applySeedRate(): void {
    if (this.form.metalRate) return;
    const metal = this.metals().find((m) => m.id === this.form.metalId);
    const purity = this.purities().find((p) => p.id === this.form.purityId);
    if (!metal) return;
    this.http.get<Array<{ metalCode: string; purityLabel?: string; ratePerGram: number }>>(
      `${environment.apiBaseUrl}/rates/ticker`
    ).subscribe({
      next: (rows) => {
        const hit = (rows || []).find((r) =>
          r.metalCode === metal.code && (!purity || r.purityLabel === purity.label)
        ) || (rows || []).find((r) => r.metalCode === metal.code);
        if (hit) {
          this.form.metalRate = Number(hit.ratePerGram);
          this.runPreview();
        }
      }
    });
  }

  addPacket(): void { if (this.packets.length < 5) this.packets.push(this.emptyPacket()); }
  removePacket(i: number): void {
    this.packets.splice(i, 1);
    if (!this.packets.length) this.packets.push(this.emptyPacket());
    this.runPreview();
  }
  addCrystal(): void { this.crystals.push(this.emptyCrystal()); }
  removeCrystal(i: number): void { this.crystals.splice(i, 1); this.runPreview(); }

  payloadBase() {
    const packetLess = this.packets.reduce((sum, p) => {
      const row = (Number(p.quantity) || 0) * (Number(p.weight) || 0);
      return p.operation === 'ADD' ? sum - row : sum + row;
    }, 0);
    return {
      quantity: this.form.quantity,
      grossWeight: this.form.grossWeight,
      pktWeight: this.form.pktWeight,
      lessWeight: Math.max(0, (Number(this.form.lessWeight) || 0) + Math.max(0, packetLess)),
      purityPercent: this.form.purityPercent,
      wastagePercent: this.form.wastagePercent,
      metalRate: this.form.metalRate,
      labCharges: this.form.labCharges,
      labChargesType: this.form.labChargesType,
      makingCharges: this.form.makingCharges,
      makingChargesType: this.form.makingChargesType,
      otherCharges: this.form.otherCharges,
      otherChargesType: this.form.otherChargesType,
      hallmarkCharges: this.form.hallmarkCharges,
      hallmarkChargesType: this.form.hallmarkChargesType,
      taxPercent: this.form.taxPercent,
      crystals: this.crystals.filter((c) => c.name?.trim())
    };
  }

  runPreview(): void {
    const body = this.payloadBase();
    if (!body.grossWeight || !body.purityPercent) { this.preview.set(null); return; }
    this.http.post<Preview>(`${environment.apiBaseUrl}/inventory/add-stock/preview`, body).subscribe({
      next: (res) => this.preview.set(res),
      error: () => this.preview.set(null)
    });
  }

  saveSetting(key: keyof StockSettings, value: string): void {
    this.http.put(`${environment.apiBaseUrl}/calc/settings`, { key, value }).subscribe({
      next: () => { this.message.set(`Setting ${key} updated`); this.reload(); this.runPreview(); },
      error: (err) => this.message.set(err?.error?.message || 'Setting update failed')
    });
  }

  save(): void {
    if (!this.branchId || !this.form.itemName?.trim()) {
      this.message.set('Branch and item name are required');
      return;
    }
    this.saving.set(true);
    const body = {
      branchId: this.branchId,
      stockItemId: this.form.stockItemId || null,
      sku: this.form.sku || null,
      itemName: this.form.itemName,
      itemCategory: this.form.itemCategory,
      stockType: this.form.stockType,
      metalId: this.form.metalId || null,
      purityId: this.form.purityId || null,
      barcodePrefix: this.form.barcodePrefix,
      barcode: this.form.barcode,
      rfidNo: this.form.rfidNo,
      hsnNo: this.form.hsnNo,
      itemSize: this.form.itemSize,
      otherInfo: [this.form.otherInfo, this.form.brandName && `Brand:${this.form.brandName}`, this.form.counterName && `Counter:${this.form.counterName}`]
        .filter(Boolean).join(' | '),
      quantity: this.form.quantity,
      grossWeight: this.form.grossWeight,
      pktWeight: this.form.pktWeight,
      lessWeight: this.form.lessWeight,
      tagWeight: this.form.tagWeight,
      purityPercent: this.form.purityPercent,
      wastagePercent: this.form.wastagePercent,
      metalRate: this.form.metalRate,
      labCharges: this.form.labCharges,
      labChargesType: this.form.labChargesType,
      makingCharges: this.form.makingCharges,
      makingChargesType: this.form.makingChargesType,
      otherCharges: this.form.otherCharges,
      otherChargesType: this.form.otherChargesType,
      hallmarkCharges: this.form.hallmarkCharges,
      hallmarkChargesType: this.form.hallmarkChargesType,
      taxPercent: this.form.taxPercent,
      packets: this.packets.filter((p) => p.description || p.weight),
      crystals: this.crystals.filter((c) => c.name?.trim())
    };
    this.http.post<StockEntry>(`${environment.apiBaseUrl}/inventory/add-stock`, body).subscribe({
      next: (res) => {
        this.message.set(`Stock added · ${res.entryNo} · ₹ ${res.finalValuation}`);
        this.form = this.emptyForm();
        this.packets = [this.emptyPacket()];
        this.crystals = [];
        this.preview.set(null);
        this.saving.set(false);
        this.reload();
      },
      error: (err) => {
        this.saving.set(false);
        this.message.set(err?.error?.message || 'Add stock failed');
      }
    });
  }
}
