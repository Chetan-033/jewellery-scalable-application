import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth.guard';
import { AuthService } from './core/auth.service';
import { LoginComponent } from './features/login/login.component';
import { ShellComponent } from './layout/shell.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { MastersComponent } from './features/masters/masters.component';
import { CustomerFormComponent } from './features/masters/customer-form/customer-form.component';
import { CustomerDashboardComponent } from './features/customers/customer-dashboard/customer-dashboard.component';
import { RatesComponent } from './features/rates/rates.component';
import { InventoryComponent } from './features/inventory/inventory.component';
import { SalesComponent } from './features/sales/sales.component';
import { PurchaseComponent } from './features/purchase/purchase.component';
import { OrdersComponent } from './features/orders/orders.component';
import { LoansComponent } from './features/loans/loans.component';
import { CreditComponent } from './features/credit/credit.component';
import { ModulePageComponent } from './features/module-page/module-page.component';

function upcoming(path: string, title: string, description: string, moduleCode: string, features: string[]) {
  return { path, component: ModulePageComponent, data: { title, description, moduleCode, features } };
}

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: () => (inject(AuthService).isAuthenticated() ? '/app/dashboard' : '/login')
  },
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  {
    path: 'app',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'masters', component: MastersComponent },
      { path: 'masters/customers/add', component: CustomerFormComponent },
      { path: 'masters/customers/:id/edit', component: CustomerFormComponent },
      { path: 'masters/customers/:id', component: CustomerDashboardComponent },
      { path: 'customers/:id', component: CustomerDashboardComponent },
      { path: 'customers/:id/dashboard', component: CustomerDashboardComponent },
      { path: 'rates', component: RatesComponent },
      { path: 'inventory', component: InventoryComponent },
      { path: 'sales', component: SalesComponent },
      { path: 'purchase', component: PurchaseComponent },
      { path: 'orders', component: OrdersComponent },
      { path: 'loans', component: LoansComponent },
      { path: 'credit', component: CreditComponent },
      upcoming('manufacturing', 'Karigar / Melting', 'Job work and refining.', 'manufacturing', [
        'Job card', 'Melting batch', 'Hallmark / repair'
      ]),
      upcoming('accounting', 'Accounts', 'Day book and statements.', 'accounting', [
        'Day Book', 'Ledgers', 'Trial balance / P&L / BS'
      ]),
      upcoming('compliance', 'GST', 'Tax reports.', 'compliance', [
        'CGST/SGST/IGST', 'GSTR summaries', 'HSN'
      ]),
      upcoming('reports', 'Reports', 'Operational reports.', 'reporting', [
        'Sales / stock / outstanding exports'
      ]),
      upcoming('admin', 'Settings', 'Firm setup and backups.', 'platform', [
        'Users / roles', 'Number series', 'Backup', 'Stock calc settings'
      ])
    ]
  },
  {
    path: '**',
    redirectTo: () => (inject(AuthService).isAuthenticated() ? '/app/dashboard' : '/login')
  }
];
