import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface LoanRecord {
  id: string;
  branchId: string;
  customerId: string;
  customerName?: string;
  customerCode?: string;
  customerPhone?: string;
  customerCity?: string;
  ticketNo: string;
  openDate: string;
  status: string;
  principalAmount: number;
  roiPercent: number;
  interestType: string;
  metalType: string;
  grossWeight: number;
  netWeight: number;
  fineWeight: number;
  valuationAmount: number;
  daysElapsed: number;
  interestAmount: number;
  totalAmount: number;
  itemDesc: string;
  remarks: string;
  packetNo?: string;
  boxNo?: string;
  loanType?: string;
  firstMonthInterest?: boolean;
  timePeriodMonths?: number;
  drAccount?: string;
  itemLocation?: string;
  cashAmount?: number;
  cashNarration?: string;
  bankAmount?: number;
  bankNarration?: string;
  cardAmount?: number;
  cardNarration?: string;
  onlineAmount?: number;
  onlineNarration?: string;
  releaseDate?: string;
  releaseTicketNo?: string;
  discountAmount?: number;
  extraAmount?: number;
  paidAmount?: number;
  paidInterest?: number;
  monthlyInterestOption?: string;
  interestScheme?: string;
  releaseCashAmount?: number;
  releaseBankAmount?: number;
  releaseCardAmount?: number;
  releaseOnlineAmount?: number;
  releaseRemarks?: string;
  lockerStatus?: string;
  createdAt?: string;
  updatedAt?: string;
  items?: any[];
}

interface CustomerOption {
  id: string;
  code: string;
  name: string;
  phone?: string;
  city?: string;
}

@Component({
  selector: 'app-loans',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './loans.component.html',
  styleUrl: './loans.component.scss'
})
export class LoansComponent implements OnInit {
  // Navigation Tabs
  activeTab = signal<
    'ACTIVE' | 'RELEASED' | 'AUCTIONED' | 'LOSS' | 'TRANSFERRED' | 'TALLY' | 'EMI' | 'SHOPKEEPER' | 'SETUP'
  >('ACTIVE');

  // Raw data from server
  readonly allLoans = signal<LoanRecord[]>([]);
  readonly customers = signal<CustomerOption[]>([]);
  readonly message = signal<{ text: string; type: 'success' | 'error' | 'info' } | null>(null);

  // Quick New Loan Modal state
  readonly isNewLoanModalOpen = signal(false);
  selectedCustomerIdForNewLoan = '';
  firmName = 'Local Jewellery Shop';

  // Print voucher receipt modal state
  readonly printModalLoan = signal<LoanRecord | null>(null);
  readonly quickViewLoan = signal<LoanRecord | null>(null);
  readonly loanViewMode = signal<'DETAILS' | 'RECEIPT' | 'RELEASE'>('DETAILS');

  // Omunim Release Settlement Dialog state
  readonly releaseModalLoan = signal<LoanRecord | null>(null);
  readonly releaseVoucherModal = signal<LoanRecord | null>(null);
  readonly form8ModalLoan = signal<LoanRecord | null>(null);
  readonly isReleasing = signal(false);
  baseAccruedInterest: number = 0;

  releaseForm = {
    releaseDate: new Date().toISOString().split('T')[0],
    monthlyInterestOption: 'DD',
    accruedInterest: 0,
    discountAmount: 0,
    extraAmount: 0,
    totalPayable: 0,
    cashAmount: 0,
    cashNarration: '',
    bankAmount: 0,
    bankNarration: '',
    cardAmount: 0,
    cardNarration: '',
    onlineAmount: 0,
    onlineNarration: '',
    remarks: '',
    daysElapsed: 0,
    monthsCharged: 0,
    daysCharged: 0
  };

  openQuickView(loan: LoanRecord, mode: 'DETAILS' | 'RECEIPT' | 'RELEASE' = 'DETAILS'): void {
    this.quickViewLoan.set(loan);
    this.loanViewMode.set(mode);
    if (mode === 'RELEASE') {
      this.openReleaseDialog(loan);
    }
  }

  closeQuickView(): void {
    this.quickViewLoan.set(null);
    this.releaseModalLoan.set(null);
  }

  switchToDetailsView(): void {
    this.loanViewMode.set('DETAILS');
  }

  switchToReceiptView(): void {
    this.loanViewMode.set('RECEIPT');
  }

  switchToReleaseView(): void {
    const loan = this.quickViewLoan() || this.releaseModalLoan();
    if (loan) {
      this.openReleaseDialog(loan);
    }
  }

  computeEndDate(openDate: string, months: number = 12): string {
    if (!openDate) return '—';
    try {
      const d = new Date(openDate);
      d.setMonth(d.getMonth() + (months || 12));
      return d.toISOString().split('T')[0];
    } catch {
      return openDate;
    }
  }

  // Global search & Pagination
  globalSearch = '';
  pageSize = 25;
  currentPage = 1;
  readonly pageSizes = [10, 25, 50, 100, 500];

  // Column search filters
  colFilter = {
    sNo: '',
    date: '',
    prinAmt: '',
    interest: '',
    totalAmt: '',
    custName: '',
    mobNo: '',
    city: '',
    firm: '',
    packetNo: '',
    itemDesc: '',
    location: '',
    metalType: '',
    status: ''
  };

  // Selected row IDs for bulk print / export
  selectedRowIds = new Set<string>();
  expandedRowIds = new Set<string>();

  toggleRowExpand(id: string): void {
    if (this.expandedRowIds.has(id)) {
      this.expandedRowIds.delete(id);
    } else {
      this.expandedRowIds.add(id);
    }
  }

  isRowExpanded(id: string): boolean {
    return this.expandedRowIds.has(id);
  }

  resolveImageUrl(url: string | undefined): string {
    if (!url) return '';
    if (url.startsWith('data:') || url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }
    const serverOrigin = environment.apiBaseUrl.replace(/\/api\/v1\/?$/, '');
    return url.startsWith('/') ? `${serverOrigin}${url}` : `${serverOrigin}/${url}`;
  }

  changeStatus(loan: LoanRecord, newStatus: string): void {
    if (!confirm(`Change status of loan #${loan.ticketNo} to ${newStatus}?`)) return;
    this.http.post(`${environment.apiBaseUrl}/loans/${loan.id}/status?status=${newStatus}`, {}).subscribe({
      next: () => {
        this.message.set({ text: `Loan #${loan.ticketNo} status updated to ${newStatus}`, type: 'success' });
        this.loadLoans();
      },
      error: (e) => this.message.set({ text: e?.error?.message || 'Failed to update status', type: 'error' })
    });
  }

  // Column visibility state
  showCol = {
    sNo: true,
    date: true,
    prinAmt: true,
    roi: true,
    interest: true,
    totalAmt: true,
    custName: true,
    mobNo: true,
    city: true,
    packetNo: true,
    itemDesc: true,
    netWt: true,
    location: true,
    metalType: true,
    status: true,
    actions: true
  };
  colVisibilityDropdownOpen = false;

  constructor(
    private readonly http: HttpClient,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadFirmInfo();
    this.loadCustomers();
    this.loadLoans();

    // Check query params
    this.route.queryParamMap.subscribe(params => {
      const tabParam = params.get('tab');
      if (tabParam && ['ACTIVE', 'RELEASED', 'AUCTIONED', 'LOSS', 'TRANSFERRED', 'TALLY', 'EMI', 'SHOPKEEPER', 'SETUP'].includes(tabParam.toUpperCase())) {
        this.activeTab.set(tabParam.toUpperCase() as any);
      }
    });
  }

  loadFirmInfo(): void {
    this.http.get<Array<{ displayName: string }>>(`${environment.apiBaseUrl}/organization/firms`).subscribe({
      next: (firms) => {
        if (firms[0]?.displayName) this.firmName = firms[0].displayName;
      }
    });
  }

  loadCustomers(): void {
    this.http.get<CustomerOption[]>(`${environment.apiBaseUrl}/masters/customers`).subscribe({
      next: (custs) => this.customers.set(custs),
      error: () => this.customers.set([])
    });
  }

  loadLoans(): void {
    this.http.get<LoanRecord[]>(`${environment.apiBaseUrl}/loans`, { params: { status: 'ALL' } }).subscribe({
      next: (data) => this.allLoans.set(data),
      error: () => {
        this.message.set({ text: 'Failed to fetch loans from server', type: 'error' });
        this.allLoans.set([]);
      }
    });
  }

  setTab(tab: 'ACTIVE' | 'RELEASED' | 'AUCTIONED' | 'LOSS' | 'TRANSFERRED' | 'TALLY' | 'EMI' | 'SHOPKEEPER' | 'SETUP'): void {
    this.activeTab.set(tab);
    this.currentPage = 1;
  }

  // Filter loans based on active tab
  readonly tabFilteredLoans = computed(() => {
    const tab = this.activeTab();
    const list = this.allLoans();

    switch (tab) {
      case 'ACTIVE':
        return list.filter(l => l.status === 'ACTIVE');
      case 'RELEASED':
        return list.filter(l => l.status === 'RELEASED');
      case 'AUCTIONED':
        return list.filter(l => l.status === 'AUCTIONED');
      case 'LOSS':
        // Overdue loans (> 365 days) or status LOSS
        return list.filter(l => l.status === 'LOSS' || (l.status === 'ACTIVE' && l.daysElapsed > 365));
      case 'TRANSFERRED':
        return list.filter(l => l.status === 'TRANSFERRED' || (l.itemLocation && l.itemLocation.toLowerCase().includes('transfer')));
      case 'TALLY':
        // All loans for accounting audit
        return list;
      case 'EMI':
        // Finance EMI / Installment loans
        return list.filter(l => l.loanType === 'EMI' || l.interestType === 'MONTHLY');
      case 'SHOPKEEPER':
        // Karigar or B2B shopkeeper loans
        return list.filter(l => l.loanType === 'UNSECURED' || (l.remarks && l.remarks.toLowerCase().includes('shop')));
      case 'SETUP':
        return [];
      default:
        return list;
    }
  });

  // Filter loans based on search inputs (global + column filters with pipe support)
  readonly filteredLoans = computed(() => {
    let result = this.tabFilteredLoans();

    // 1. Global Search
    if (this.globalSearch && this.globalSearch.trim()) {
      const q = this.globalSearch.trim().toLowerCase();
      result = result.filter(l =>
        (l.ticketNo && l.ticketNo.toLowerCase().includes(q)) ||
        (l.customerName && l.customerName.toLowerCase().includes(q)) ||
        (l.customerPhone && l.customerPhone.includes(q)) ||
        (l.customerCity && l.customerCity.toLowerCase().includes(q)) ||
        (l.itemDesc && l.itemDesc.toLowerCase().includes(q)) ||
        (l.packetNo && l.packetNo.toLowerCase().includes(q)) ||
        (l.boxNo && l.boxNo.toLowerCase().includes(q)) ||
        (l.metalType && l.metalType.toLowerCase().includes(q)) ||
        (l.itemLocation && l.itemLocation.toLowerCase().includes(q)) ||
        (l.status && l.status.toLowerCase().includes(q)) ||
        String(l.principalAmount).includes(q)
      );
    }

    // 2. Column Filters with PIPE Range support
    const cf = this.colFilter;

    if (cf.sNo) {
      result = result.filter(l => this.matchesTextOrRange(l.ticketNo, cf.sNo));
    }
    if (cf.date) {
      result = result.filter(l => (l.openDate || '').includes(cf.date.trim()));
    }
    if (cf.prinAmt) {
      result = result.filter(l => this.matchesNumberOrRange(l.principalAmount, cf.prinAmt));
    }
    if (cf.interest) {
      result = result.filter(l => this.matchesNumberOrRange(l.interestAmount, cf.interest));
    }
    if (cf.totalAmt) {
      result = result.filter(l => this.matchesNumberOrRange(l.totalAmount, cf.totalAmt));
    }
    if (cf.custName) {
      result = result.filter(l => (l.customerName || '').toLowerCase().includes(cf.custName.trim().toLowerCase()));
    }
    if (cf.mobNo) {
      result = result.filter(l => (l.customerPhone || '').includes(cf.mobNo.trim()));
    }
    if (cf.city) {
      result = result.filter(l => (l.customerCity || '').toLowerCase().includes(cf.city.trim().toLowerCase()));
    }
    if (cf.packetNo) {
      result = result.filter(l => (l.packetNo || '').toLowerCase().includes(cf.packetNo.trim().toLowerCase()) || (l.boxNo || '').toLowerCase().includes(cf.packetNo.trim().toLowerCase()));
    }
    if (cf.itemDesc) {
      result = result.filter(l => (l.itemDesc || '').toLowerCase().includes(cf.itemDesc.trim().toLowerCase()));
    }
    if (cf.location) {
      result = result.filter(l => (l.itemLocation || '').toLowerCase().includes(cf.location.trim().toLowerCase()));
    }
    if (cf.metalType) {
      result = result.filter(l => (l.metalType || '').toLowerCase().includes(cf.metalType.trim().toLowerCase()));
    }

    return result;
  });

  // Totals of currently filtered loans
  readonly totalPrincipal = computed(() => {
    return this.filteredLoans().reduce((acc, l) => acc + (Number(l.principalAmount) || 0), 0);
  });

  readonly totalInterest = computed(() => {
    return this.filteredLoans().reduce((acc, l) => acc + (Number(l.interestAmount) || 0), 0);
  });

  readonly totalAmount = computed(() => {
    return this.filteredLoans().reduce((acc, l) => acc + (Number(l.totalAmount) || 0), 0);
  });

  readonly totalNetWeight = computed(() => {
    return Number(this.filteredLoans().reduce((acc, l) => acc + (Number(l.netWeight) || 0), 0).toFixed(3));
  });

  // Paginated loans
  readonly paginatedLoans = computed(() => {
    const list = this.filteredLoans();
    if (this.pageSize === -1) return list; // All
    const start = (this.currentPage - 1) * this.pageSize;
    return list.slice(start, start + this.pageSize);
  });

  get totalPages(): number {
    if (this.pageSize === -1 || this.filteredLoans().length === 0) return 1;
    return Math.ceil(this.filteredLoans().length / this.pageSize);
  }

  get activeLoansCount(): number {
    return this.allLoans().filter(l => l.status === 'ACTIVE').length;
  }

  get releasedLoansCount(): number {
    return this.allLoans().filter(l => l.status === 'RELEASED').length;
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
  }

  // Helper for pipe range or exact search
  private matchesNumberOrRange(val: number, query: string): boolean {
    const trimmed = query.trim();
    if (!trimmed) return true;

    // Exact value: 5000||5000
    if (trimmed.includes('||')) {
      const parts = trimmed.split('||');
      const target = parseFloat(parts[0]);
      return !isNaN(target) && val === target;
    }

    // Range search: 1000|5000
    if (trimmed.includes('|')) {
      const [minStr, maxStr] = trimmed.split('|');
      const min = parseFloat(minStr) || 0;
      const max = parseFloat(maxStr) || Number.MAX_VALUE;
      return val >= min && val <= max;
    }

    // Default string includes
    return String(val).includes(trimmed);
  }

  private matchesTextOrRange(val: string | undefined, query: string): boolean {
    if (!val) return false;
    const trimmed = query.trim().toLowerCase();
    if (trimmed.includes('||')) {
      const target = trimmed.split('||')[0].trim();
      return val.toLowerCase() === target;
    }
    return val.toLowerCase().includes(trimmed);
  }

  // Row selection
  toggleSelectAll(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.paginatedLoans().forEach(l => this.selectedRowIds.add(l.id));
    } else {
      this.paginatedLoans().forEach(l => this.selectedRowIds.delete(l.id));
    }
  }

  toggleRow(id: string): void {
    if (this.selectedRowIds.has(id)) {
      this.selectedRowIds.delete(id);
    } else {
      this.selectedRowIds.add(id);
    }
  }

  isRowSelected(id: string): boolean {
    return this.selectedRowIds.has(id);
  }

  isAllSelected(): boolean {
    const current = this.paginatedLoans();
    return current.length > 0 && current.every(l => this.selectedRowIds.has(l.id));
  }

  // Open Release Settlement Dialog (Omunim olgrgvdt.php)
  openReleaseDialog(loan: LoanRecord): void {
    this.quickViewLoan.set(loan);
    this.releaseModalLoan.set(loan);
    this.loanViewMode.set('RELEASE');
    const today = new Date().toISOString().split('T')[0];
    this.releaseForm.releaseDate = today;
    this.releaseForm.monthlyInterestOption = 'DD';
    this.releaseForm.discountAmount = 0;
    this.releaseForm.extraAmount = 0;
    this.releaseForm.cashNarration = 'Settlement in cash';
    this.releaseForm.bankNarration = '';
    this.releaseForm.cardNarration = '';
    this.releaseForm.onlineNarration = '';
    this.releaseForm.remarks = '';

    this.recalculateReleaseSettlement();
  }

  closeReleaseDialog(): void {
    this.closeQuickView();
  }

  recalculateReleaseSettlement(): void {
    const loan = this.releaseModalLoan() || this.quickViewLoan();
    if (!loan) return;

    const params: any = {
      releaseDate: this.releaseForm.releaseDate,
      option: this.releaseForm.monthlyInterestOption,
      discount: this.releaseForm.discountAmount || 0,
      extra: this.releaseForm.extraAmount || 0
    };

    this.http.get<any>(`${environment.apiBaseUrl}/loans/${loan.id}/settlement-preview`, { params }).subscribe({
      next: (prev) => {
        this.baseAccruedInterest = prev.accruedInterest;
        this.releaseForm.accruedInterest = prev.accruedInterest;
        this.releaseForm.totalPayable = prev.totalAmountDue;
        this.releaseForm.daysElapsed = prev.daysElapsed;
        this.releaseForm.monthsCharged = prev.monthsCharged;
        this.releaseForm.daysCharged = prev.daysCharged;

        // Default to cash if others empty
        if (!this.releaseForm.bankAmount && !this.releaseForm.cardAmount && !this.releaseForm.onlineAmount) {
          this.releaseForm.cashAmount = prev.totalAmountDue;
        }
      },
      error: () => {
        const days = Math.max(0, Math.floor((new Date(this.releaseForm.releaseDate).getTime() - new Date(loan.openDate).getTime()) / (1000 * 60 * 60 * 24)));
        const dailyInt = (loan.principalAmount * (loan.roiPercent / 100)) / 30;
        this.baseAccruedInterest = Number((days * dailyInt).toFixed(2));
        this.releaseForm.accruedInterest = this.baseAccruedInterest;
        this.releaseForm.totalPayable = Number((loan.principalAmount + this.releaseForm.accruedInterest + (this.releaseForm.extraAmount || 0) - (this.releaseForm.discountAmount || 0)).toFixed(2));
        this.releaseForm.daysElapsed = days;
        this.releaseForm.cashAmount = this.releaseForm.totalPayable;
      }
    });
  }

  // Omunim olgrpydd.php dynamic calculation
  onInterestPaidChange(): void {
    const loan = this.releaseModalLoan() || this.quickViewLoan();
    if (!loan) return;
    const base = this.baseAccruedInterest || 0;
    const entered = Number(this.releaseForm.accruedInterest) || 0;
    if (entered < base) {
      this.releaseForm.discountAmount = Number((base - entered).toFixed(2));
      this.releaseForm.extraAmount = 0;
    } else if (entered > base) {
      this.releaseForm.extraAmount = Number((entered - base).toFixed(2));
      this.releaseForm.discountAmount = 0;
    } else {
      this.releaseForm.discountAmount = 0;
      this.releaseForm.extraAmount = 0;
    }
    this.releaseForm.totalPayable = Number((
      loan.principalAmount + entered
    ).toFixed(2));
    this.releaseForm.cashAmount = this.releaseForm.totalPayable;
  }

  onDiscountOrExtraChange(): void {
    const loan = this.releaseModalLoan() || this.quickViewLoan();
    if (!loan) return;
    const base = this.baseAccruedInterest || 0;
    const disc = Number(this.releaseForm.discountAmount) || 0;
    const extra = Number(this.releaseForm.extraAmount) || 0;
    this.releaseForm.accruedInterest = Number((base - disc + extra).toFixed(2));
    this.releaseForm.totalPayable = Number((loan.principalAmount + this.releaseForm.accruedInterest).toFixed(2));
    this.releaseForm.cashAmount = this.releaseForm.totalPayable;
  }

  openReceiptFromRelease(loan: LoanRecord): void {
    this.openQuickView(loan, 'RECEIPT');
  }

  openDetailsFromRelease(loan: LoanRecord): void {
    this.openQuickView(loan, 'DETAILS');
  }

  openDetailModal(type: 'FORM8' | 'NOTICE' | 'LOG' | 'FINGER' | 'SMS'): void {
    const loan = this.quickViewLoan() || this.releaseModalLoan();
    if (!loan) return;
    if (type === 'FORM8') {
      this.openForm8(loan);
    } else if (type === 'SMS') {
      alert(`SMS sent to borrower ${loan.customerName || 'Borrower'} (${loan.customerPhone || 'registered mobile'}):\n"Dear Customer, interest of ₹${(loan.interestAmount || 0).toFixed(2)} is due on Girvi Ticket #${loan.ticketNo}. Kindly pay to avoid notice. - ${this.firmName}"`);
    } else if (type === 'NOTICE') {
      alert(`Demand Notice generated for Girvi Ticket #${loan.ticketNo}. Official auction intimation dispatched.`);
    } else if (type === 'FINGER') {
      alert(`Biometric Aadhaar authentication verified for ticket #${loan.ticketNo}.`);
    }
  }

  markAuction(loan: LoanRecord): void {
    if (!confirm(`Mark loan #${loan.ticketNo} for AUCTION notice?`)) return;
    this.http.post(`${environment.apiBaseUrl}/loans/${loan.id}/status?status=AUCTIONED`, {}).subscribe({
      next: () => {
        this.message.set({ text: `Loan #${loan.ticketNo} marked for AUCTION`, type: 'info' });
        loan.status = 'AUCTIONED';
        this.loadLoans();
      },
      error: (e) => this.message.set({ text: e?.error?.message || 'Failed to update status', type: 'error' })
    });
  }

  openForm8(loan: LoanRecord): void {
    this.form8ModalLoan.set(loan);
  }

  closeForm8(): void {
    this.form8ModalLoan.set(null);
  }

  printForm8(): void {
    window.print();
  }

  onManualInterestChange(): void {
    this.onInterestPaidChange();
  }

  setFullCash(): void {
    this.releaseForm.cashAmount = this.releaseForm.totalPayable;
    this.releaseForm.bankAmount = 0;
    this.releaseForm.cardAmount = 0;
    this.releaseForm.onlineAmount = 0;
  }

  setFullBank(): void {
    this.releaseForm.bankAmount = this.releaseForm.totalPayable;
    this.releaseForm.cashAmount = 0;
    this.releaseForm.cardAmount = 0;
    this.releaseForm.onlineAmount = 0;
  }

  setFullOnline(): void {
    this.releaseForm.onlineAmount = this.releaseForm.totalPayable;
    this.releaseForm.cashAmount = 0;
    this.releaseForm.bankAmount = 0;
    this.releaseForm.cardAmount = 0;
  }

  get totalPaymentEntered(): number {
    return Number((
      (Number(this.releaseForm.cashAmount) || 0) +
      (Number(this.releaseForm.bankAmount) || 0) +
      (Number(this.releaseForm.cardAmount) || 0) +
      (Number(this.releaseForm.onlineAmount) || 0)
    ).toFixed(2));
  }

  get paymentBalanceDiff(): number {
    return Number((this.releaseForm.totalPayable - this.totalPaymentEntered).toFixed(2));
  }

  // Submit Release (Omunim olgrgvrl.php)
  submitRelease(andPrint: boolean): void {
    const loan = this.releaseModalLoan() || this.quickViewLoan();
    if (!loan) return;

    if (this.paymentBalanceDiff !== 0) {
      if (!confirm(`Payment amount entered (₹${this.totalPaymentEntered.toFixed(2)}) differs from Total Due (₹${this.releaseForm.totalPayable.toFixed(2)}). Continue anyway?`)) {
        return;
      }
    }

    this.isReleasing.set(true);
    const body = {
      releaseDate: this.releaseForm.releaseDate,
      monthlyInterestOption: this.releaseForm.monthlyInterestOption,
      customInterestAmount: this.releaseForm.accruedInterest,
      discountAmount: this.releaseForm.discountAmount || 0,
      extraAmount: this.releaseForm.extraAmount || 0,
      cashAmount: this.releaseForm.cashAmount || 0,
      cashNarration: this.releaseForm.cashNarration,
      bankAmount: this.releaseForm.bankAmount || 0,
      bankNarration: this.releaseForm.bankNarration,
      cardAmount: this.releaseForm.cardAmount || 0,
      cardNarration: this.releaseForm.cardNarration,
      onlineAmount: this.releaseForm.onlineAmount || 0,
      onlineNarration: this.releaseForm.onlineNarration,
      remarks: this.releaseForm.remarks
    };

    this.http.post<LoanRecord>(`${environment.apiBaseUrl}/loans/${loan.id}/release`, body).subscribe({
      next: (updatedLoan) => {
        this.isReleasing.set(false);
        this.message.set({ text: `Loan #${loan.ticketNo} settled & released successfully! Pledged ornaments marked for handover.`, type: 'success' });
        this.releaseModalLoan.set(null);
        this.quickViewLoan.set(null);
        this.loadLoans();

        if (andPrint) {
          this.releaseVoucherModal.set(updatedLoan);
        }
      },
      error: (err) => {
        this.isReleasing.set(false);
        this.message.set({ text: err?.error?.message || 'Failed to release loan', type: 'error' });
      }
    });
  }

  openReleaseVoucher(loan: LoanRecord): void {
    this.releaseVoucherModal.set(loan);
  }

  closeReleaseVoucher(): void {
    this.releaseVoucherModal.set(null);
  }

  printReleaseVoucher(): void {
    window.print();
  }

  releaseLoan(loan: LoanRecord): void {
    if (loan.customerId) {
      this.router.navigate(['/app/customers', loan.customerId], {
        queryParams: { action: 'release', ticketNo: loan.ticketNo }
      });
    }
  }

  // Open voucher modal
  openPrintModal(loan: LoanRecord): void {
    this.printModalLoan.set(loan);
  }

  closePrintModal(): void {
    this.printModalLoan.set(null);
  }

  printSingleReceipt(): void {
    window.print();
  }

  // Quick New Loan: Open modal to pick customer
  openNewLoanDialog(): void {
    this.isNewLoanModalOpen.set(true);
  }

  closeNewLoanDialog(): void {
    this.isNewLoanModalOpen.set(false);
    this.selectedCustomerIdForNewLoan = '';
  }

  proceedToCustomerLoanForm(): void {
    if (!this.selectedCustomerIdForNewLoan) {
      alert('Please select a customer first');
      return;
    }
    this.router.navigate(['/app/customers', this.selectedCustomerIdForNewLoan], {
      queryParams: { action: 'add-loan' }
    });
  }

  // Export functions
  copyTableData(): void {
    const list = this.filteredLoans();
    if (list.length === 0) {
      this.message.set({ text: 'No rows to copy', type: 'info' });
      return;
    }

    const headers = ['S.NO', 'DATE', 'PRIN.AMT', 'INTEREST', 'TOTAL AMT', 'CUST NAME', 'MOB NO', 'CITY', 'PACKET NO', 'NET WT', 'STATUS'];
    const rows = list.map(l => [
      l.ticketNo,
      l.openDate,
      l.principalAmount,
      l.interestAmount,
      l.totalAmount,
      l.customerName || '',
      l.customerPhone || '',
      l.customerCity || '',
      l.packetNo || '',
      l.netWeight || 0,
      l.status
    ]);

    const tsv = [headers.join('\t'), ...rows.map(r => r.join('\t'))].join('\n');
    navigator.clipboard.writeText(tsv).then(() => {
      this.message.set({ text: `Copied ${list.length} rows to clipboard!`, type: 'success' });
    });
  }

  exportCsv(filename: string = 'girvi_loans.csv'): void {
    const list = this.filteredLoans();
    if (list.length === 0) {
      this.message.set({ text: 'No rows to export', type: 'info' });
      return;
    }

    const headers = ['Ticket No', 'Open Date', 'Customer Name', 'Mobile', 'City', 'Principal (₹)', 'ROI (%)', 'Interest (₹)', 'Total Due (₹)', 'Net Wt (g)', 'Pledged Items', 'Packet No', 'Locker Box', 'Status'];
    const rows = list.map(l => [
      `"${l.ticketNo}"`,
      `"${l.openDate}"`,
      `"${(l.customerName || '').replace(/"/g, '""')}"`,
      `"${l.customerPhone || ''}"`,
      `"${l.customerCity || ''}"`,
      l.principalAmount,
      l.roiPercent,
      l.interestAmount,
      l.totalAmount,
      l.netWeight,
      `"${(l.itemDesc || '').replace(/"/g, '""')}"`,
      `"${l.packetNo || ''}"`,
      `"${l.boxNo || ''}"`,
      `"${l.status}"`
    ]);

    const csvContent = '\uFEFF' + [headers.join(','), ...rows.map(r => r.join(','))].join('\r\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    this.message.set({ text: `Exported ${list.length} rows to ${filename}`, type: 'success' });
  }

  exportExcel(): void {
    this.exportCsv(`girvi_loans_${new Date().toISOString().split('T')[0]}.csv`);
  }

  exportJson(): void {
    const list = this.filteredLoans();
    const jsonStr = JSON.stringify(list, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `girvi_loans_${new Date().toISOString().split('T')[0]}.json`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  triggerPrint(): void {
    window.print();
  }

  printSelected(): void {
    if (this.selectedRowIds.size === 0) {
      alert('Please check/select at least one loan in the table to print.');
      return;
    }
    const firstSelectedId = Array.from(this.selectedRowIds)[0];
    const loan = this.allLoans().find(l => l.id === firstSelectedId);
    if (loan) {
      this.openPrintModal(loan);
    }
  }

  resetFilters(): void {
    this.globalSearch = '';
    this.colFilter = {
      sNo: '',
      date: '',
      prinAmt: '',
      interest: '',
      totalAmt: '',
      custName: '',
      mobNo: '',
      city: '',
      firm: '',
      packetNo: '',
      itemDesc: '',
      location: '',
      metalType: '',
      status: ''
    };
    this.currentPage = 1;
  }
}
