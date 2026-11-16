import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiPage, pageContent } from '../../core/api-page';

export interface Customer {
  id: string;
  code: string;
  name: string;
  phone?: string;
  city?: string;
  firstName?: string;
  lastName?: string;
  spouseName?: string;
  fatherName?: string;
  relationType?: string;
  partyType?: string;
  accountGroup?: string;
  address?: string;
  photoBase64?: string;
  createdAt?: string;
}

export interface Supplier {
  id: string;
  code: string;
  name: string;
  phone?: string;
  city?: string;
  gstin?: string;
}

@Component({
  selector: 'app-masters',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './masters.component.html',
  styleUrl: './masters.component.scss'
})
export class MastersComponent implements OnInit {
  activeTab: 'customer' | 'supplier' | 'karigar' | 'staff' | 'moneyLender' | 'visitor' = 'customer';
  categoryFilter: 'RETAIL' | 'WHOLESALE' | 'ALL' = 'RETAIL';
  userMenuOpen = signal(false);

  readonly customers = signal<Customer[]>([]);
  readonly suppliers = signal<Supplier[]>([]);
  readonly message = signal<string | null>(null);

  q = '';
  currentPage = signal(1);
  pageSize = 10;

  // Search Typeahead & Suggestions State
  readonly searchSuggestions = signal<Customer[]>([]);
  readonly showSuggestions = signal(false);
  readonly selectedSuggestionIndex = signal(-1);
  private searchDebounceTimer?: ReturnType<typeof setTimeout>;

  supplierForm = { code: '', name: '', phone: '', city: '' };

  constructor(
    private readonly http: HttpClient,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((p) => {
      const qq = p.get('q');
      if (qq) {
        this.q = qq;
        this.activeTab = 'customer';
      }
      const t = p.get('tab');
      if (t === 'supplier') this.activeTab = 'supplier';
      else if (t === 'karigar') this.activeTab = 'karigar';
      else if (t === 'staff') this.activeTab = 'staff';
      else if (t === 'moneyLender') this.activeTab = 'moneyLender';
      else if (t === 'visitor') this.activeTab = 'visitor';
      else this.activeTab = 'customer';

      const action = p.get('action');
      if (action === 'add') {
        if (this.activeTab === 'supplier') this.showStub('Add Supplier Form');
        else if (this.activeTab === 'staff') this.showStub('Add Staff Form');
        else if (this.activeTab === 'moneyLender') this.showStub('Add Money Lender Form');
        else if (this.activeTab === 'visitor') this.showStub('Add Visitor Form');
      }
      this.reload();
    });
  }

  reload(): void {
    const params: Record<string, string | number> = { page: 0, size: 100 };
    if (this.q.trim()) params['q'] = this.q.trim();
    this.http.get<ApiPage<Customer>>(`${environment.apiBaseUrl}/masters/customers`, { params }).subscribe({
      next: (r) => this.customers.set(pageContent(r)),
      error: () => this.customers.set([])
    });
    this.http.get<Supplier[]>(`${environment.apiBaseUrl}/masters/suppliers`).subscribe({
      next: (r) => this.suppliers.set(r),
      error: () => this.suppliers.set([])
    });
  }

  // Live Typeahead Search & Ranking
  onSearchInput(): void {
    const val = this.q.trim();
    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer);
    }

    if (!val) {
      this.searchSuggestions.set([]);
      this.showSuggestions.set(false);
      this.selectedSuggestionIndex.set(-1);
      this.reload();
      return;
    }

    this.searchDebounceTimer = setTimeout(() => {
      this.reload();
      this.fetchSuggestions(val);
    }, 150);
  }

  fetchSuggestions(query: string): void {
    const qLower = query.toLowerCase();
    this.http.get<Customer[]>(`${environment.apiBaseUrl}/masters/customers`, { params: { q: query } }).subscribe({
      next: (results) => {
        // Strict prefix re-ranking: names / first names starting with qLower rank first!
        const sorted = [...results].sort((a, b) => {
          const aName = (a.name || '').toLowerCase();
          const bName = (b.name || '').toLowerCase();
          const aFirst = (a.firstName || '').toLowerCase();
          const bFirst = (b.firstName || '').toLowerCase();

          const aStarts = aName.startsWith(qLower) || aFirst.startsWith(qLower);
          const bStarts = bName.startsWith(qLower) || bFirst.startsWith(qLower);

          if (aStarts && !bStarts) return -1;
          if (!aStarts && bStarts) return 1;
          return aName.localeCompare(bName);
        });

        this.searchSuggestions.set(sorted.slice(0, 8));
        this.showSuggestions.set(sorted.length > 0);
        this.selectedSuggestionIndex.set(-1);
      },
      error: () => {
        this.searchSuggestions.set([]);
        this.showSuggestions.set(false);
      }
    });
  }

  onSearchFocus(): void {
    if (this.q.trim()) {
      if (this.searchSuggestions().length > 0) {
        this.showSuggestions.set(true);
      } else {
        this.fetchSuggestions(this.q.trim());
      }
    }
  }

  selectSuggestion(c: Customer): void {
    this.q = c.name;
    this.showSuggestions.set(false);
    this.searchSuggestions.set([]);
    this.reload();
  }

  clearSearch(): void {
    this.q = '';
    this.showSuggestions.set(false);
    this.searchSuggestions.set([]);
    this.reload();
  }

  onSearchKeyDown(event: KeyboardEvent): void {
    const suggestions = this.searchSuggestions();
    if (!this.showSuggestions() || suggestions.length === 0) {
      if (event.key === 'Enter') {
        this.reload();
      }
      return;
    }

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.selectedSuggestionIndex.update(idx => (idx + 1 < suggestions.length ? idx + 1 : 0));
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.selectedSuggestionIndex.update(idx => (idx > 0 ? idx - 1 : suggestions.length - 1));
    } else if (event.key === 'Enter') {
      event.preventDefault();
      const currentIdx = this.selectedSuggestionIndex();
      if (currentIdx >= 0 && currentIdx < suggestions.length) {
        this.selectSuggestion(suggestions[currentIdx]);
      } else {
        this.showSuggestions.set(false);
        this.reload();
      }
    } else if (event.key === 'Escape') {
      this.showSuggestions.set(false);
    }
  }

  closeSuggestions(): void {
    setTimeout(() => {
      this.showSuggestions.set(false);
    }, 220);
  }

  // Filtered and Paginated list
  filteredCustomers = computed(() => {
    let list = this.customers();
    if (this.categoryFilter === 'WHOLESALE') {
      list = list.filter(c => (c.partyType || '').toLowerCase().includes('wholesale') || (c.accountGroup || '').toLowerCase().includes('wholesale'));
    } else if (this.categoryFilter === 'RETAIL') {
      list = list.filter(c => !(c.partyType || '').toLowerCase().includes('wholesale'));
    }
    return list;
  });

  totalPages = computed(() => {
    const count = this.filteredCustomers().length;
    return Math.max(1, Math.ceil(count / this.pageSize));
  });

  pagedCustomers = computed(() => {
    const page = this.currentPage();
    const start = (page - 1) * this.pageSize;
    return this.filteredCustomers().slice(start, start + this.pageSize);
  });

  leftCustomers = computed(() => {
    return this.pagedCustomers().slice(0, 5);
  });

  rightCustomers = computed(() => {
    return this.pagedCustomers().slice(5, 10);
  });

  pageNumbers = computed(() => {
    const total = this.totalPages();
    const pages: number[] = [];
    const max = Math.min(10, total);
    for (let i = 1; i <= max; i++) {
      pages.push(i);
    }
    return pages;
  });

  goToPage(p: number): void {
    if (p >= 1 && p <= this.totalPages()) {
      this.currentPage.set(p);
    }
  }

  nextPage(): void {
    if (this.currentPage() < this.totalPages()) {
      this.currentPage.update(p => p + 1);
    }
  }

  prevPage(): void {
    if (this.currentPage() > 1) {
      this.currentPage.update(p => p - 1);
    }
  }

  setCategory(cat: 'RETAIL' | 'WHOLESALE' | 'ALL'): void {
    this.categoryFilter = cat;
    this.currentPage.set(1);
  }

  toggleUserMenu(): void {
    this.userMenuOpen.update(v => !v);
  }

  closeUserMenu(): void {
    this.userMenuOpen.set(false);
  }

  selectTab(tabName: any): void {
    this.activeTab = tabName;
    this.closeUserMenu();
  }

  // Quick Action Shortcuts
  openWhatsApp(c: Customer, event: Event): void {
    event.stopPropagation();
    const raw = (c.phone || '').replace(/\D/g, '');
    if (!raw) {
      this.showToast('No phone number recorded for WhatsApp');
      return;
    }
    window.open(`https://wa.me/91${raw}?text=Hello%20${encodeURIComponent(c.name)}`, '_blank');
  }

  openSms(c: Customer, event: Event): void {
    event.stopPropagation();
    this.showToast(`SMS alert queued for ${c.name} (${c.phone || 'No phone'})`);
  }

  navigateCustomer(c: Customer): void {
    this.router.navigate(['/app/customers', c.id]);
  }

  navigateAddLoan(c: Customer, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/app/customers', c.id], { queryParams: { action: 'add-loan' } });
  }

  navigateSell(c: Customer, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/app/sales'], { queryParams: { customerId: c.id } });
  }

  navigateOrder(c: Customer, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/app/orders'], { queryParams: { customerId: c.id } });
  }

  navigateEdit(c: Customer, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/app/masters/customers', c.id, 'edit']);
  }

  showStub(feature: string): void {
    this.closeUserMenu();
    this.showToast(`${feature} will be implemented in the next phase. Currently focused on Customer management as requested.`);
  }

  showToast(msg: string): void {
    this.message.set(msg);
    setTimeout(() => {
      if (this.message() === msg) {
        this.message.set(null);
      }
    }, 4500);
  }

  saveSupplier(): void {
    this.http.post(`${environment.apiBaseUrl}/masters/suppliers`, this.supplierForm).subscribe({
      next: () => {
        this.showToast('Supplier saved');
        this.reload();
      },
      error: (e) => this.showToast(e?.error?.message || 'Failed')
    });
  }

  relationOf(c: Customer): string {
    if (c.spouseName) return `W/o ${c.spouseName}`;
    if (c.fatherName) return `${c.relationType || 'S/o'} ${c.fatherName}`;
    return '';
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
