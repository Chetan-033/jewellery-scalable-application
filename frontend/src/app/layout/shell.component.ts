import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../core/auth.service';
import { environment } from '../../environments/environment';

interface RateTick {
  metalCode: string;
  metalName: string;
  purityLabel?: string;
  ratePerGram: number;
  ratePerTenGram: number;
}

interface NavItem {
  path: string;
  label: string;
  icon: string;
  exact?: boolean;
  hasDropdown?: boolean;
}

interface NavGroup {
  title: string;
  items: NavItem[];
}

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent implements OnInit, OnDestroy {
  readonly firmName = signal('Jewellery Shop');
  readonly apiOk = signal(false);
  readonly rates = signal<RateTick[]>([]);
  readonly clock = signal(new Date());
  readonly sidebarCollapsed = signal(false);
  readonly mobileNavOpen = signal(false);
  readonly userFlyoutOpen = signal(false);
  readonly userFlyoutTop = signal<number>(200);
  globalQuery = '';
  private timer?: ReturnType<typeof setInterval>;

  readonly navGroups: NavGroup[] = [
    {
      title: 'Overview',
      items: [
        { path: '/app/dashboard', label: 'Dashboard', icon: '⌂', exact: true },
        { path: '/app/rates', label: 'Metal Rates', icon: '₹' },
        { path: '/app/reports', label: 'Reports', icon: '▦' }
      ]
    },
    {
      title: 'Operations',
      items: [
        { path: '/app/sales', label: 'Sales', icon: '◆' },
        { path: '/app/purchase', label: 'Purchase', icon: '◇' },
        { path: '/app/orders', label: 'Orders', icon: '▤' },
        { path: '/app/inventory', label: 'Inventory', icon: '▣' }
      ]
    },
    {
      title: 'Finance',
      items: [
        { path: '/app/loans', label: 'Loans / Girvi', icon: '◈' },
        { path: '/app/credit', label: 'Credit / Udhaar', icon: '◎' },
        { path: '/app/accounting', label: 'Accounts', icon: '▣' },
        { path: '/app/compliance', label: 'GST', icon: '☰' }
      ]
    },
    {
      title: 'Setup',
      items: [
        { path: '/app/masters', label: 'User List', icon: '👤', hasDropdown: true },
        { path: '/app/admin', label: 'Settings', icon: '⋯' }
      ]
    }
  ];

  constructor(
    readonly auth: AuthService,
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.timer = setInterval(() => this.clock.set(new Date()), 1000);
    this.auth.refreshMe().subscribe({ error: () => undefined });
    this.http.get<{ status: string }>(`${environment.apiBaseUrl}/health`).subscribe({
      next: (h) => this.apiOk.set(h.status === 'UP'),
      error: () => this.apiOk.set(false)
    });
    this.http.get<Array<{ displayName: string }>>(`${environment.apiBaseUrl}/organization/firms`).subscribe({
      next: (firms) => { if (firms[0]) this.firmName.set(firms[0].displayName); }
    });
    this.reloadRates();

    const saved = localStorage.getItem('jewellery_erp_sidebar');
    if (saved === 'collapsed') this.sidebarCollapsed.set(true);
  }

  ngOnDestroy(): void {
    if (this.timer) clearInterval(this.timer);
  }

  toggleSidebar(): void {
    this.sidebarCollapsed.update((v) => !v);
    localStorage.setItem(
      'jewellery_erp_sidebar',
      this.sidebarCollapsed() ? 'collapsed' : 'expanded'
    );
  }

  closeMobileNav(): void {
    this.mobileNavOpen.set(false);
    this.closeUserFlyout();
  }

  toggleUserFlyout(event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();
    const target = (event.currentTarget as HTMLElement).closest('.nav-item-container');
    if (target) {
      const rect = target.getBoundingClientRect();
      const flyoutH = 340;
      let top = rect.top - 40;
      if (top + flyoutH > window.innerHeight) {
        top = Math.max(15, window.innerHeight - flyoutH - 20);
      }
      this.userFlyoutTop.set(Math.max(15, top));
    }
    this.userFlyoutOpen.update((v) => !v);
  }

  closeUserFlyout(): void {
    this.userFlyoutOpen.set(false);
  }

  navigateAndClose(path: string, queryParams?: Record<string, string>): void {
    this.closeUserFlyout();
    this.closeMobileNav();
    void this.router.navigate([path], { queryParams });
  }

  reloadRates(): void {
    this.http.get<RateTick[]>(`${environment.apiBaseUrl}/rates/ticker`).subscribe({
      next: (rows) => this.rates.set(rows || []),
      error: () => this.rates.set([])
    });
  }

  searchGlobal(): void {
    const q = this.globalQuery.trim();
    if (!q) return;
    this.closeMobileNav();
    void this.router.navigate(['/app/masters'], { queryParams: { q } });
  }

  rateLabel(r: RateTick): string {
    const purity = r.purityLabel ? ` ${r.purityLabel}` : '';
    return `${r.metalCode}${purity}`;
  }

  rateValue(r: RateTick): string {
    return `₹${Number(r.ratePerTenGram).toFixed(0)}/10g`;
  }

  userInitials(): string {
    const name = this.auth.user()?.displayName || this.auth.user()?.username || 'A';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[1][0]).toUpperCase();
  }
}
