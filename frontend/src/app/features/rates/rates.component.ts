import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface Metal {
  id: string;
  code: string;
  name: string;
}

interface RateRow {
  id: string;
  metalId: string;
  purityId?: string;
  branchId?: string;
  ratePerGram: number;
  currencyCode: string;
  effectiveFrom: string;
  source: string;
}

@Component({
  selector: 'app-rates',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rates.component.html',
  styleUrl: './rates.component.scss'
})
export class RatesComponent implements OnInit {
  readonly metals = signal<Metal[]>([]);
  readonly rates = signal<RateRow[]>([]);
  readonly message = signal<string | null>(null);
  branchId = '';
  form = { metalId: '', ratePerGram: 0 };

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<Array<{ id: string }>>(`${environment.apiBaseUrl}/organization/branches`).subscribe({
      next: (rows) => {
        if (rows[0]) {
          this.branchId = rows[0].id;
        }
      }
    });
    this.http.get<Metal[]>(`${environment.apiBaseUrl}/masters/metals`).subscribe({
      next: (rows) => {
        this.metals.set(rows);
        if (rows[0]) {
          this.form.metalId = rows[0].id;
          this.loadRates();
        }
      }
    });
  }

  loadRates(): void {
    if (!this.form.metalId) {
      return;
    }
    this.http
      .get<RateRow[]>(`${environment.apiBaseUrl}/rates/metal`, { params: { metalId: this.form.metalId } })
      .subscribe({
        next: (rows) => this.rates.set(rows),
        error: () => this.message.set('Could not load rates')
      });
  }

  save(): void {
    this.http
      .post(`${environment.apiBaseUrl}/rates/metal`, {
        metalId: this.form.metalId,
        branchId: this.branchId || null,
        ratePerGram: this.form.ratePerGram
      })
      .subscribe({
        next: () => {
          this.message.set('Rate saved (historical bills keep their snapshot later)');
          this.loadRates();
        },
        error: (err) => this.message.set(err?.error?.message || 'Rate save failed')
      });
  }

  metalName(id: string): string {
    return this.metals().find((m) => m.id === id)?.name || id;
  }
}
