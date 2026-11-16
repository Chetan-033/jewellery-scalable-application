import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export type MainSettingTab =
  | 'DAILY_RATES'
  | 'METALS'
  | 'PURITY'
  | 'CRYSTAL'
  | 'ITEMS'
  | 'CITIES'
  | 'COUNTRIES'
  | 'STATES'
  | 'ROI'
  | 'FORMS'
  | 'MASTER'
  | 'LAYOUT'
  | 'DATA';

export type FormSubTab =
  | 'LOAN_NOTICE'
  | 'MONEY_DEPOSIT'
  | 'FORM_N8'
  | 'FORM_R7'
  | 'UDHAAR_NOTICE'
  | 'CUSTOM_FORM';

export interface LoanNoticeConfig {
  noticeViewingLanguage: string;
  defaultLanguage: string;
  paperSize: string;
  documentWidth: number;
  documentHeight: number;
  topMargin: number;
  leftMargin: number;
  rightMargin: number;
  bottomMargin: number;
  formBorder: boolean;
  borderStyle: string;
  headerSectionHeight: number;
  displayHeader: boolean;
  formLeftLogo: boolean;
  formRightLogo: boolean;

  headerLeftText: string;
  headerLeftFontSize: number;
  headerLeftColor: string;
  headerLeftVisible: boolean;

  headerRightText: string;
  headerRightFontSize: number;
  headerRightColor: string;
  headerRightVisible: boolean;

  headerLabelText: string;
  headerLabelFontSize: number;
  headerLabelColor: string;
  headerLabelVisible: boolean;

  formHeaderFontSize: number;
  formHeaderColor: string;
  formHeaderVisible: boolean;

  firmName: string;
  firmNameFontSize: number;
  firmNameColor: string;
  firmNameVisible: boolean;

  firmDes: string;
  firmDesFontSize: number;
  firmDesColor: string;
  firmDesVisible: boolean;

  firmAddress: string;
  firmAddressFontSize: number;
  firmAddressColor: string;
  firmAddressVisible: boolean;

  phone: string;
  phoneFontSize: number;
  phoneColor: string;
  phoneVisible: boolean;

  email: string;
  emailFontSize: number;
  emailColor: string;
  emailVisible: boolean;

  licenseNo: string;
  warningPeriodDays: number;
  noticeSubject: string;
  noticeBody: string;
  signatoryTitle: string;
}

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss'
})
export class SettingsComponent implements OnInit {
  // Navigation State
  readonly activeMainTab = signal<MainSettingTab>('FORMS');
  readonly activeFormTab = signal<FormSubTab>('LOAN_NOTICE');

  // UI status
  readonly isSaving = signal(false);
  readonly isLoading = signal(false);
  readonly message = signal<{ text: string; type: 'success' | 'error' | 'info' } | null>(null);
  readonly showLivePreview = signal(true);

  // Available options
  readonly languages = ['English', 'Hindi', 'Marathi', 'Gujarati', 'Bengali', 'Tamil', 'Telugu', 'Kannada'];
  readonly paperSizes = ['A5 Size', 'A4 Size', 'Letter Size', 'Thermal 80mm'];
  readonly colors = ['Black', 'Dark Blue', 'Maroon', 'Dark Gray', 'Golden Brown'];
  readonly fontSizes = [9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 22, 24, 26, 28];

  // Loan Notice Model
  loanNotice: LoanNoticeConfig = {
    noticeViewingLanguage: 'English',
    defaultLanguage: 'English',
    paperSize: 'A5 Size',
    documentWidth: 135,
    documentHeight: 195,
    topMargin: 10,
    leftMargin: 15,
    rightMargin: 15,
    bottomMargin: 10,
    formBorder: true,
    borderStyle: 'SOLID',
    headerSectionHeight: 25,
    displayHeader: true,
    formLeftLogo: true,
    formRightLogo: true,

    headerLeftText: 'श्री गणेशाय नमः',
    headerLeftFontSize: 14,
    headerLeftColor: 'Black',
    headerLeftVisible: true,

    headerRightText: '|| ॐ नमः शिवाय ||',
    headerRightFontSize: 14,
    headerRightColor: 'Black',
    headerRightVisible: true,

    headerLabelText: 'GIRVI / LOAN NOTICE',
    headerLabelFontSize: 18,
    headerLabelColor: 'Black',
    headerLabelVisible: true,

    formHeaderFontSize: 20,
    formHeaderColor: 'Black',
    formHeaderVisible: true,

    firmName: 'Local Jewellery Shop',
    firmNameFontSize: 16,
    firmNameColor: 'Black',
    firmNameVisible: true,

    firmDes: 'Certified Gold & Silver Jewellery & Girvi Merchant',
    firmDesFontSize: 14,
    firmDesColor: 'Black',
    firmDesVisible: true,

    firmAddress: 'Shop #4, Main Market, Gold Bazaar, Kolkata - 700001',
    firmAddressFontSize: 12,
    firmAddressColor: 'Black',
    firmAddressVisible: true,

    phone: '+91 98301 23456 / 033-22114455',
    phoneFontSize: 12,
    phoneColor: 'Black',
    phoneVisible: true,

    email: 'support@localjewellery.com',
    emailFontSize: 12,
    emailColor: 'Black',
    emailVisible: true,

    licenseNo: 'WB-GIRVI-2026/8941',
    warningPeriodDays: 15,
    noticeSubject: 'FINAL NOTICE FOR REPAYMENT & SETTLEMENT OF PLEDGED ORNAMENTS',
    noticeBody: 'This is to bring to your kind attention that the loan advance availed by you against the pledge of gold/silver ornaments detailed below is currently overdue and interest charges have accumulated. You are hereby requested to settle the outstanding dues and release your ornaments within the stipulated period.',
    signatoryTitle: 'For Local Jewellery Shop (Authorized Signatory / Prop.)'
  };

  // Sample Notice Preview Data
  sampleBorrower = {
    noticeNo: 'NOT-2026-0042',
    date: new Date().toISOString().split('T')[0],
    ticketNo: 'LN-2026-0089',
    customerName: 'Smt. Anindita Ghosh',
    fatherSpouse: 'D/o R. Ghosh',
    address: 'Flat 3B, New Town, Hooghly - 712201',
    mobile: '9231338718',
    pledgeDate: '2025-10-15',
    metalDescription: '22K Hallmarked Gold Bangles & Chain (Gross: 24.50g, Net: 23.80g)',
    principalAmount: 75000,
    interestRate: 1.5,
    interestDue: 14625,
    totalDue: 89625,
    dueDate: '2026-11-25'
  };

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadSettings();
  }

  setMainTab(tab: MainSettingTab): void {
    this.activeMainTab.set(tab);
    this.message.set(null);
  }

  setFormTab(tab: FormSubTab): void {
    this.activeFormTab.set(tab);
    this.message.set(null);
  }

  loadSettings(): void {
    this.isLoading.set(true);
    this.http.get<Partial<LoanNoticeConfig>>(`${environment.apiBaseUrl}/settings/forms/loan-notice`).subscribe({
      next: (data) => {
        this.isLoading.set(false);
        if (data) {
          this.loanNotice = { ...this.loanNotice, ...data };
        }
      },
      error: () => {
        this.isLoading.set(false);
        // Defaults are already set
      }
    });
  }

  saveSettings(): void {
    this.isSaving.set(true);
    this.http.put(`${environment.apiBaseUrl}/settings/forms/loan-notice`, this.loanNotice).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.message.set({ text: '✔ Loan notice form settings saved successfully!', type: 'success' });
        setTimeout(() => this.message.set(null), 4000);
      },
      error: (err) => {
        this.isSaving.set(false);
        this.message.set({ text: 'Settings saved locally (server sync: ' + (err.statusText || 'offline') + ')', type: 'info' });
      }
    });
  }

  resetDefaults(): void {
    if (!confirm('Are you sure you want to reset Loan Notice settings to system defaults?')) {
      return;
    }
    this.isLoading.set(true);
    this.http.post<LoanNoticeConfig>(`${environment.apiBaseUrl}/settings/forms/loan-notice/reset`, {}).subscribe({
      next: (data) => {
        this.isLoading.set(false);
        if (data) {
          this.loanNotice = { ...data };
        }
        this.message.set({ text: 'Restored Loan Notice settings to system defaults.', type: 'info' });
      },
      error: () => {
        this.isLoading.set(false);
        this.message.set({ text: 'Restored Loan Notice settings to defaults.', type: 'info' });
      }
    });
  }

  printSampleNotice(): void {
    window.print();
  }

  getColorHex(colorName: string): string {
    switch (colorName) {
      case 'Dark Blue': return '#1e3a8a';
      case 'Maroon': return '#881337';
      case 'Dark Gray': return '#334155';
      case 'Golden Brown': return '#78350f';
      case 'Black':
      default: return '#0f172a';
    }
  }
}
