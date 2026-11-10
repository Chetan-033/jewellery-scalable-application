import { Component, ElementRef, OnInit, ViewChild, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

export interface PledgedItemForm {
  metalType: string;
  itemName: string;
  quantity: number;
  grossWeight: number;
  lessWeight: number;
  netWeight: number;
  purityPercent: number;
  fineWeight: number;
  valuationAmount: number;
  photoUrl: string;
}

export interface GirviLoanRecord {
  id: string;
  ticketNo: string;
  openDate: string;
  status: string;
  customerId: string;
  branchId: string;
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
  customerName?: string;
  customerPhone?: string;
  customerCity?: string;
  cashAmount?: number;
  interestScheme?: string;
  releaseDate?: string;
  releaseTicketNo?: string;
  discountAmount?: number;
  extraAmount?: number;
  paidAmount?: number;
  paidInterest?: number;
  monthlyInterestOption?: string;
  releaseCashAmount?: number;
  releaseBankAmount?: number;
  releaseCardAmount?: number;
  releaseOnlineAmount?: number;
  releaseRemarks?: string;
  lockerStatus?: string;
  createdAt?: string;
  updatedAt?: string;
  items?: PledgedItemForm[];
}

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './customer-dashboard.component.html',
  styleUrl: './customer-dashboard.component.scss'
})
export class CustomerDashboardComponent implements OnInit {
  @ViewChild('webcamVideo') webcamVideo?: ElementRef<HTMLVideoElement>;

  readonly customer = signal<any | null>(null);
  readonly loans = signal<GirviLoanRecord[]>([]);
  readonly activeTab = signal<'loans' | 'overview' | 'sell' | 'orders' | 'credit'>('loans');
  readonly isAddLoanOpen = signal(false);
  readonly isSaving = signal(false);
  readonly message = signal<{ text: string; type: 'success' | 'error' | 'info' } | null>(null);

  // Print modal state
  readonly printLoanModal = signal<GirviLoanRecord | null>(null);
  readonly selectedLoan = signal<GirviLoanRecord | null>(null);
  readonly loanViewMode = signal<'DETAILS' | 'RELEASE' | 'RECEIPT' | 'RELEASED_DETAILS'>('DETAILS');
  readonly activeDetailModal = signal<'NONE' | 'FORM8' | 'NOTICE' | 'LOG' | 'FINGER' | 'SMS'>('NONE');
  readonly loanFilter = signal<'ALL' | 'ACTIVE' | 'RELEASED'>('ALL');
  readonly isLoanDropdownOpen = signal(false);

  readonly filteredLoans = computed(() => {
    const f = this.loanFilter();
    const list = this.loans();
    if (f === 'ACTIVE') return list.filter(l => l.status === 'ACTIVE');
    if (f === 'RELEASED') return list.filter(l => l.status === 'RELEASED');
    return list;
  });

  // Omunim Release Settlement Dialog state
  readonly releaseModalLoan = signal<GirviLoanRecord | null>(null);
  readonly releaseVoucherModal = signal<GirviLoanRecord | null>(null);
  readonly form8ModalLoan = signal<GirviLoanRecord | null>(null);
  readonly isReleasing = signal(false);
  baseAccruedInterest: number = 0;

  releaseForm = {
    releaseDate: new Date().toISOString().split('T')[0],
    monthlyInterestOption: 'DD', // DD, FM, HM, 7D
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

  // Webcam state
  readonly webcamOpen = signal(false);
  activeItemIndexForPhoto: number | null = null;
  private mediaStream: MediaStream | null = null;

  branchId = '';
  firmName = 'Local Jewellery Shop';
  pendingReleaseTicket: string | null = null;
  pendingViewTicket: string | null = null;

  // Add Loan Form Model
  loanForm = this.emptyLoanForm();

  readonly metalTypes = ['GOLD', 'SILVER', 'DIAMOND', 'PLATINUM'];

  constructor(
    private readonly http: HttpClient,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadCustomer(id);
        this.loadCustomerLoans(id);
      }
    });

    // Check query params if ?action=add-loan or ?action=release or ?ticketNo=... is passed
    this.route.queryParamMap.subscribe(params => {
      const act = params.get('action');
      const view = params.get('view');
      const ticket = params.get('ticketNo');

      if (act === 'add-loan') {
        this.isAddLoanOpen.set(true);
        this.activeTab.set('loans');
      } else if (act === 'release') {
        this.activeTab.set('loans');
        this.isAddLoanOpen.set(false);
        this.pendingReleaseTicket = ticket;
        if (this.loans().length > 0) {
          const match = ticket ? this.loans().find(l => l.ticketNo === ticket) : this.loans().find(l => l.status === 'ACTIVE');
          if (match) {
            this.openReleaseDialog(match);
            this.pendingReleaseTicket = null;
          }
        }
      } else if (ticket) {
        this.activeTab.set('loans');
        this.isAddLoanOpen.set(false);
        this.pendingViewTicket = ticket;
        if (this.loans().length > 0) {
          const match = this.loans().find(l => l.ticketNo === ticket);
          if (match) {
            this.selectLoan(match);
            if (view === 'released' || match.status === 'RELEASED') {
              this.loanViewMode.set('RELEASED_DETAILS');
            }
            this.pendingViewTicket = null;
          }
        }
      } else if (view === 'released') {
        this.activeTab.set('loans');
        this.isAddLoanOpen.set(false);
        if (this.loans().length > 0) {
          const match = this.loans().find(l => l.status === 'RELEASED') || this.loans()[0];
          if (match) {
            this.selectLoan(match);
            this.loanViewMode.set('RELEASED_DETAILS');
          }
        }
      }
    });

    this.http.get<Array<{ id: string; name?: string }>>(`${environment.apiBaseUrl}/organization/branches`).subscribe({
      next: (branches) => {
        if (branches[0]) this.branchId = branches[0].id;
      }
    });

    this.http.get<Array<{ displayName: string }>>(`${environment.apiBaseUrl}/organization/firms`).subscribe({
      next: (firms) => {
        if (firms[0]) this.firmName = firms[0].displayName;
      }
    });
  }

  loadCustomer(id: string): void {
    this.http.get<any>(`${environment.apiBaseUrl}/masters/customers/${id}`).subscribe({
      next: (cust) => this.customer.set(cust),
      error: () => this.message.set({ text: 'Customer not found', type: 'error' })
    });
  }

  loadCustomerLoans(customerId: string): void {
    this.http.get<GirviLoanRecord[]>(`${environment.apiBaseUrl}/loans/customer/${customerId}`).subscribe({
      next: (data) => {
        this.loans.set(data);
        if (this.pendingReleaseTicket) {
          const match = data.find(l => l.ticketNo === this.pendingReleaseTicket);
          if (match) {
            this.openReleaseDialog(match);
            this.pendingReleaseTicket = null;
            return;
          }
        }
        if (this.pendingViewTicket) {
          const match = data.find(l => l.ticketNo === this.pendingViewTicket);
          if (match) {
            this.selectLoan(match);
            const viewParam = this.route.snapshot.queryParamMap.get('view');
            if (viewParam === 'released' || match.status === 'RELEASED') {
              this.loanViewMode.set('RELEASED_DETAILS');
            }
            this.pendingViewTicket = null;
            return;
          }
        }
        if (!this.selectedLoan() && data.length > 0) {
          const active = data.find(l => l.status === 'ACTIVE') || data[0];
          this.selectLoan(active);
        } else if (this.selectedLoan()) {
          const updated = data.find(l => l.id === this.selectedLoan()!.id);
          if (updated) {
            this.selectedLoan.set(updated);
            if (updated.status === 'RELEASED') {
              this.loanViewMode.set('RELEASED_DETAILS');
            }
          }
        }
      },
      error: () => this.loans.set([])
    });
  }

  emptyLoanForm() {
    const today = new Date().toISOString().split('T')[0];
    return {
      principalAmount: null as number | null,
      roiPercent: 2.5,
      interestType: 'MONTHLY',
      loanType: 'SECURED',
      openDate: today,
      packetNo: '',
      boxNo: '',
      firstMonthInterest: false,
      timePeriodMonths: 12,
      drAccount: 'Secured Loans',
      itemLocation: '',
      remarks: '',
      cashAmount: null as number | null,
      cashNarration: 'Cash disbursed on pledge',
      bankAmount: null as number | null,
      bankNarration: '',
      cardAmount: null as number | null,
      cardNarration: '',
      onlineAmount: null as number | null,
      onlineNarration: '',
      items: [
        this.emptyPledgedItem('GOLD', 'Gold Ornament')
      ]
    };
  }

  emptyPledgedItem(metal: string = 'GOLD', name: string = ''): PledgedItemForm {
    return {
      metalType: metal,
      itemName: name,
      quantity: 1,
      grossWeight: 0,
      lessWeight: 0,
      netWeight: 0,
      purityPercent: 91.6,
      fineWeight: 0,
      valuationAmount: 0,
      photoUrl: ''
    };
  }

  openAddLoan(): void {
    this.loanForm = this.emptyLoanForm();
    this.isAddLoanOpen.set(true);
  }

  closeAddLoan(): void {
    this.isAddLoanOpen.set(false);
  }

  onLoanTypeChange(): void {
    if (this.loanForm.loanType === 'UNSECURED') {
      this.loanForm.items = [];
      this.loanForm.packetNo = '';
      this.loanForm.boxNo = '';
      this.loanForm.drAccount = 'Unsecured Loans';
    } else {
      if (this.loanForm.items.length === 0) {
        this.loanForm.items = [this.emptyPledgedItem('GOLD', 'Gold Ornament')];
      }
      this.loanForm.drAccount = 'Secured Loans';
    }
  }

  addPledgedItem(): void {
    this.loanForm.items.push(this.emptyPledgedItem('GOLD', ''));
  }

  removePledgedItem(index: number): void {
    if (this.loanForm.items.length > 1) {
      this.loanForm.items.splice(index, 1);
    }
  }

  onItemWeightChange(item: PledgedItemForm): void {
    const gross = Number(item.grossWeight) || 0;
    const less = Number(item.lessWeight) || 0;
    item.netWeight = Math.max(0, Number((gross - less).toFixed(4)));

    const purity = Number(item.purityPercent) || 0;
    item.fineWeight = Number(((item.netWeight * purity) / 100).toFixed(4));
  }

  // Summary getters for current form
  get totalItemsCount(): number {
    return this.loanForm.items.reduce((sum, item) => sum + (Number(item.quantity) || 1), 0);
  }

  get totalGrossWeight(): number {
    return Number(this.loanForm.items.reduce((sum, item) => sum + (Number(item.grossWeight) || 0), 0).toFixed(4));
  }

  get totalNetWeight(): number {
    return Number(this.loanForm.items.reduce((sum, item) => sum + (Number(item.netWeight) || 0), 0).toFixed(4));
  }

  get totalLessWeight(): number {
    return Number(this.loanForm.items.reduce((sum, item) => sum + (Number(item.lessWeight) || 0), 0).toFixed(4));
  }

  get totalFineWeight(): number {
    return Number(this.loanForm.items.reduce((sum, item) => sum + (Number(item.fineWeight) || 0), 0).toFixed(4));
  }

  get totalItemValuation(): number {
    return Number(this.loanForm.items.reduce((sum, item) => sum + (Number(item.valuationAmount) || 0), 0).toFixed(2));
  }

  // Active loans summary for statistics cards
  get activeLoansCount(): number {
    return this.loans().filter(l => l.status === 'ACTIVE').length;
  }

  get totalActivePrincipal(): number {
    return this.loans()
      .filter(l => l.status === 'ACTIVE')
      .reduce((sum, l) => sum + (Number(l.principalAmount) || 0), 0);
  }

  get totalAccruedInterest(): number {
    return this.loans()
      .filter(l => l.status === 'ACTIVE')
      .reduce((sum, l) => sum + (Number(l.interestAmount) || 0), 0);
  }

  get totalPledgedNetWeight(): number {
    return this.loans()
      .filter(l => l.status === 'ACTIVE')
      .reduce((sum, l) => sum + (Number(l.netWeight) || 0), 0);
  }

  // Save Girvi Loan
  submitLoan(andPrint: boolean = false): void {
    const cust = this.customer();
    if (!cust) return;

    const principal = Number(this.loanForm.principalAmount);
    if (!principal || principal <= 0) {
      this.message.set({ text: 'Please enter a valid Principal Amount greater than zero.', type: 'error' });
      return;
    }

    if (!this.branchId) {
      this.message.set({ text: 'Branch not loaded. Please wait a moment and try again.', type: 'error' });
      return;
    }

    this.isSaving.set(true);

    const isUnsecured = this.loanForm.loanType === 'UNSECURED';
    const payload = {
      branchId: this.branchId,
      customerId: cust.id,
      openDate: this.loanForm.openDate,
      principalAmount: principal,
      roiPercent: this.loanForm.roiPercent,
      interestType: this.loanForm.interestType,
      metalType: isUnsecured ? 'UNSECURED' : (this.loanForm.items[0]?.metalType || 'GOLD'),
      grossWeight: isUnsecured ? 0 : this.totalGrossWeight,
      netWeight: isUnsecured ? 0 : this.totalNetWeight,
      fineWeight: isUnsecured ? 0 : this.totalFineWeight,
      valuationAmount: isUnsecured ? 0 : this.totalItemValuation,
      packetNo: isUnsecured ? undefined : (this.loanForm.packetNo || undefined),
      boxNo: isUnsecured ? undefined : (this.loanForm.boxNo || undefined),
      loanType: this.loanForm.loanType,
      firstMonthInterest: this.loanForm.firstMonthInterest,
      timePeriodMonths: this.loanForm.timePeriodMonths,
      drAccount: isUnsecured ? 'Unsecured Loans' : this.loanForm.drAccount,
      itemLocation: isUnsecured ? undefined : this.loanForm.itemLocation,
      itemDesc: isUnsecured ? 'Unsecured Loan' : undefined,
      cashAmount: this.loanForm.cashAmount || (principal > 0 ? principal : 0),
      cashNarration: this.loanForm.cashNarration,
      bankAmount: this.loanForm.bankAmount || 0,
      bankNarration: this.loanForm.bankNarration,
      cardAmount: this.loanForm.cardAmount || 0,
      cardNarration: this.loanForm.cardNarration,
      onlineAmount: this.loanForm.onlineAmount || 0,
      onlineNarration: this.loanForm.onlineNarration,
      remarks: this.loanForm.remarks,
      items: isUnsecured ? [] : this.loanForm.items.map(item => ({
        metalType: item.metalType,
        itemName: item.itemName || 'Pledged Item',
        quantity: Number(item.quantity) || 1,
        grossWeight: Number(item.grossWeight) || 0,
        lessWeight: Number(item.lessWeight) || 0,
        netWeight: Number(item.netWeight) || 0,
        purityPercent: Number(item.purityPercent) || 0,
        fineWeight: Number(item.fineWeight) || 0,
        valuationAmount: Number(item.valuationAmount) || 0,
        photoUrl: item.photoUrl || undefined
      }))
    };

    this.http.post<GirviLoanRecord>(`${environment.apiBaseUrl}/loans`, payload).subscribe({
      next: (createdLoan) => {
        this.isSaving.set(false);
        this.message.set({
          text: `Girvi Loan Ticket #${createdLoan.ticketNo} created successfully!`,
          type: 'success'
        });
        this.loadCustomerLoans(cust.id);
        this.isAddLoanOpen.set(false);
        this.selectedLoan.set(createdLoan);

        if (andPrint) {
          this.printLoanModal.set(createdLoan);
        }
      },
      error: (err) => {
        this.isSaving.set(false);
        this.message.set({
          text: err?.error?.message || 'Failed to create loan. Please check the inputs.',
          type: 'error'
        });
      }
    });
  }

  // Open Release Settlement Dialog (Omunim olgrgvdt.php)
  openReleaseDialog(loan: GirviLoanRecord): void {
    this.selectedLoan.set(loan);
    this.loanViewMode.set('RELEASE');
    this.releaseModalLoan.set(loan);
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
    this.releaseModalLoan.set(null);
    this.loanViewMode.set('DETAILS');
  }

  switchToDetailsView(): void {
    this.loanViewMode.set('DETAILS');
  }

  switchToReceiptView(): void {
    this.loanViewMode.set('RECEIPT');
  }

  switchToReleaseView(): void {
    const loan = this.selectedLoan();
    if (loan) {
      this.openReleaseDialog(loan);
    }
  }

  recalculateReleaseSettlement(): void {
    const loan = this.selectedLoan() || this.releaseModalLoan();
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

        // Default to cash if other modes are empty
        if (!this.releaseForm.bankAmount && !this.releaseForm.cardAmount && !this.releaseForm.onlineAmount) {
          this.releaseForm.cashAmount = prev.totalAmountDue;
        }
      },
      error: () => {
        // Fallback calculation
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
    const loan = this.selectedLoan() || this.releaseModalLoan();
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
    const loan = this.selectedLoan() || this.releaseModalLoan();
    if (!loan) return;
    const base = this.baseAccruedInterest || 0;
    const disc = Number(this.releaseForm.discountAmount) || 0;
    const extra = Number(this.releaseForm.extraAmount) || 0;
    this.releaseForm.accruedInterest = Number((base - disc + extra).toFixed(2));
    this.releaseForm.totalPayable = Number((loan.principalAmount + this.releaseForm.accruedInterest).toFixed(2));
    this.releaseForm.cashAmount = this.releaseForm.totalPayable;
  }

  openReceiptFromRelease(loan: GirviLoanRecord): void {
    this.selectLoan(loan);
    this.switchToReceiptView();
  }

  openDetailsFromRelease(loan: GirviLoanRecord): void {
    this.selectLoan(loan);
    this.switchToDetailsView();
  }

  openForm8(loan: GirviLoanRecord): void {
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
    const loan = this.selectedLoan() || this.releaseModalLoan();
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

    this.http.post<GirviLoanRecord>(`${environment.apiBaseUrl}/loans/${loan.id}/release`, body).subscribe({
      next: (updatedLoan) => {
        this.isReleasing.set(false);
        this.message.set({ text: `Loan #${loan.ticketNo} settled & released successfully! Pledged ornaments marked for handover.`, type: 'success' });
        this.releaseModalLoan.set(null);

        // Update local object
        loan.status = 'RELEASED';
        loan.releaseDate = updatedLoan.releaseDate || this.releaseForm.releaseDate;
        loan.totalAmount = updatedLoan.totalAmount || this.releaseForm.totalPayable;
        loan.interestAmount = updatedLoan.interestAmount || this.releaseForm.accruedInterest;
        loan.paidAmount = updatedLoan.paidAmount || this.releaseForm.totalPayable;
        loan.paidInterest = updatedLoan.paidInterest || this.releaseForm.accruedInterest;
        loan.discountAmount = this.releaseForm.discountAmount || 0;
        loan.extraAmount = this.releaseForm.extraAmount || 0;

        this.selectedLoan.set(loan);
        this.loanViewMode.set('RELEASED_DETAILS');

        if (this.customer()) {
          this.loadCustomerLoans(this.customer().id);
        }

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

  closeReleaseVoucher(): void {
    this.releaseVoucherModal.set(null);
  }

  printReleaseVoucher(): void {
    window.print();
  }

  // Legacy direct release shorthand
  releaseLoan(loan: GirviLoanRecord): void {
    this.openReleaseDialog(loan);
  }

  selectLoan(loan: GirviLoanRecord): void {
    this.selectedLoan.set(loan);
    if (loan.status === 'RELEASED') {
      this.loanViewMode.set('RELEASED_DETAILS');
    } else {
      this.loanViewMode.set('DETAILS');
    }
    this.isAddLoanOpen.set(false);
  }

  unreleaseLoan(loan: GirviLoanRecord): void {
    if (!confirm(`Are you sure you want to UNRELEASE Ticket #${loan.ticketNo} and mark it as ACTIVE again?`)) {
      return;
    }
    this.http.post<GirviLoanRecord>(`${environment.apiBaseUrl}/loans/${loan.id}/status?status=ACTIVE`, {}).subscribe({
      next: () => {
        this.message.set({ text: `Loan #${loan.ticketNo} unreleased and restored to ACTIVE status!`, type: 'success' });
        loan.status = 'ACTIVE';
        this.selectedLoan.set(loan);
        this.loanViewMode.set('DETAILS');
        if (this.customer()) {
          this.loadCustomerLoans(this.customer().id);
        }
      },
      error: (err) => {
        this.message.set({ text: err?.error?.message || 'Failed to unrelease loan', type: 'error' });
      }
    });
  }

  deleteLoan(loan: GirviLoanRecord): void {
    if (!confirm(`Are you sure you want to DELETE loan record #${loan.ticketNo}? This cannot be undone.`)) {
      return;
    }
    this.http.post<GirviLoanRecord>(`${environment.apiBaseUrl}/loans/${loan.id}/status?status=DELETED`, {}).subscribe({
      next: () => {
        this.message.set({ text: `Loan #${loan.ticketNo} deleted successfully.`, type: 'info' });
        this.selectedLoan.set(null);
        if (this.customer()) {
          this.loadCustomerLoans(this.customer().id);
        }
      },
      error: (err) => {
        this.message.set({ text: err?.error?.message || 'Failed to delete loan', type: 'error' });
      }
    });
  }

  closeSelectedLoan(): void {
    this.selectedLoan.set(null);
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

  openDetailModal(type: 'FORM8' | 'NOTICE' | 'LOG' | 'FINGER' | 'SMS'): void {
    this.activeDetailModal.set(type);
  }

  closeDetailModal(): void {
    this.activeDetailModal.set('NONE');
  }

  markAuction(loan: GirviLoanRecord): void {
    if (!confirm(`Mark loan #${loan.ticketNo} for AUCTION notice?`)) return;
    this.http.post(`${environment.apiBaseUrl}/loans/${loan.id}/status?status=AUCTIONED`, {}).subscribe({
      next: () => {
        this.message.set({ text: `Loan #${loan.ticketNo} marked for AUCTION`, type: 'info' });
        loan.status = 'AUCTIONED';
        if (this.customer()) this.loadCustomerLoans(this.customer().id);
      },
      error: (e) => this.message.set({ text: e?.error?.message || 'Failed to update status', type: 'error' })
    });
  }

  sendSmsReminder(loan: GirviLoanRecord): void {
    alert(`SMS sent to borrower ${this.customer()?.name} (${this.customer()?.phone || 'registered mobile'}):\n"Dear Customer, interest of ₹${loan.interestAmount.toFixed(2)} is due on Girvi Ticket #${loan.ticketNo}. Kindly pay to avoid notice. - ${this.firmName}"`);
    this.closeDetailModal();
  }

  // View / Print ticket
  openPrintModal(loan: GirviLoanRecord): void {
    this.printLoanModal.set(loan);
  }

  closePrintModal(): void {
    this.printLoanModal.set(null);
  }

  printReceipt(): void {
    window.print();
  }

  // Webcam for pledged items
  openItemWebcam(index: number): void {
    this.activeItemIndexForPhoto = index;
    this.webcamOpen.set(true);
    navigator.mediaDevices?.getUserMedia({ video: { facingMode: 'environment', width: 640, height: 480 } })
      .then(stream => {
        this.mediaStream = stream;
        setTimeout(() => {
          if (this.webcamVideo) {
            this.webcamVideo.nativeElement.srcObject = stream;
          }
        }, 100);
      })
      .catch(() => {
        alert('Webcam not accessible. Please select a photo from your computer.');
        this.webcamOpen.set(false);
      });
  }

  captureItemWebcam(): void {
    if (!this.webcamVideo?.nativeElement || this.activeItemIndexForPhoto === null) return;
    const video = this.webcamVideo.nativeElement;
    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;
    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
      const dataUrl = canvas.toDataURL('image/jpeg', 0.85);
      this.loanForm.items[this.activeItemIndexForPhoto].photoUrl = dataUrl;
    }
    this.stopItemWebcam();
  }

  stopItemWebcam(): void {
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach(t => t.stop());
      this.mediaStream = null;
    }
    this.webcamOpen.set(false);
    this.activeItemIndexForPhoto = null;
  }

  onItemFileSelected(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const reader = new FileReader();
      reader.onload = () => {
        this.loanForm.items[index].photoUrl = reader.result as string;
      };
      reader.readAsDataURL(input.files[0]);
    }
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

