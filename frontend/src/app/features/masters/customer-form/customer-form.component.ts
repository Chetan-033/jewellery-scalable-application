import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { CustomerValidationConfigService } from '../../../core/customer-validation/customer-validation-config.service';
import {
  FieldErrorMap,
  isFormatValid,
  normalizeAadhaar,
  normalizeCustomerForSave,
  validateCustomer,
  validateCustomerField
} from '../../../core/customer-validation/customer-validators';
import { CustomerFieldKey } from '../../../core/customer-validation/customer-field-rules';
import { EnterNextFieldDirective } from '../../../shared/enter-next-field.directive';

export interface CustomerFormData {
  id?: string;
  code: string;
  partyType: string;
  hos: string;
  accountGroup: string;
  title: string;
  firstName: string;
  lastName: string;
  relationType: string;
  fatherName: string;
  motherName: string;
  gender: 'M' | 'F' | '';
  dobDay: string;
  dobMonth: string;
  dobYear: string;
  spouseName: string;
  spouseDobDay: string;
  spouseDobMonth: string;
  spouseDobYear: string;
  caste: string;
  qualification: string;
  maritalStatus: string;
  annivDay: string;
  annivMonth: string;
  annivYear: string;
  // PAN / Bank
  panNo: string;
  gstin: string;
  taxNo: string;
  aadhaarNo: string;
  bankName: string;
  bankAccName: string;
  bankAccNo: string;
  bankIfscCode: string;
  occupation: string;
  annualIncome: string;
  nomineeName: string;
  nomineeRelation: string;
  designation: string;
  loginId: string;
  portalPassword: string;
  masterPassword: string;
  // Address
  businessName: string;
  officeAddress: string;
  permanentAddress: string;
  currentAddress: string;
  shippingAddress: string;
  village: string;
  wardNumber: string;
  tehsil: string;
  city: string;
  countryName: string;
  pincode: string;
  stateName: string;
  otherInfo: string;
  phone: string;
  email: string;
  // Images
  photoBase64: string;
  aadhaarFrontBase64: string;
  aadhaarFrontDesc: string;
  aadhaarBackBase64: string;
  aadhaarBackDesc: string;
  sendWelcomeSms: boolean;
}

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, EnterNextFieldDirective],
  templateUrl: './customer-form.component.html',
  styleUrl: './customer-form.component.scss'
})
export class CustomerFormComponent implements OnInit, OnDestroy {
  @ViewChild('videoElement') videoElement?: ElementRef<HTMLVideoElement>;

  private readonly validationConfig = inject(CustomerValidationConfigService);

  readonly isEdit = signal(false);
  readonly customerId = signal<string | null>(null);
  readonly message = signal<{ text: string; type: 'success' | 'error' | 'info' } | null>(null);
  readonly isSaving = signal(false);
  readonly aadhaarValidationMsg = signal<string | null>(null);
  readonly fieldErrors = signal<FieldErrorMap>({});

  // Webcam modal state
  readonly webcamOpen = signal(false);
  activeWebcamTarget: 'photo' | 'aadhaarFront' | 'aadhaarBack' | null = null;
  private mediaStream: MediaStream | null = null;

  // Form Model
  form: CustomerFormData = this.emptyForm();

  // Date select options
  readonly days = Array.from({ length: 31 }, (_, i) => String(i + 1).padStart(2, '0'));
  readonly months = [
    { num: '01', name: 'JAN' }, { num: '02', name: 'FEB' }, { num: '03', name: 'MAR' },
    { num: '04', name: 'APR' }, { num: '05', name: 'MAY' }, { num: '06', name: 'JUN' },
    { num: '07', name: 'JUL' }, { num: '08', name: 'AUG' }, { num: '09', name: 'SEP' },
    { num: '10', name: 'OCT' }, { num: '11', name: 'NOV' }, { num: '12', name: 'DEC' }
  ];
  readonly years: string[] = [];

  readonly indianStates = [
    'Andhra Pradesh', 'Arunachal Pradesh', 'Assam', 'Bihar', 'Chhattisgarh', 'Goa',
    'Gujarat', 'Haryana', 'Himachal Pradesh', 'Jharkhand', 'Karnataka', 'Kerala',
    'Madhya Pradesh', 'Maharashtra', 'Manipur', 'Meghalaya', 'Mizoram', 'Nagaland',
    'Odisha', 'Punjab', 'Rajasthan', 'Sikkim', 'Tamil Nadu', 'Telangana', 'Tripura',
    'Uttar Pradesh', 'Uttarakhand', 'West Bengal', 'Delhi', 'Jammu & Kashmir', 'Ladakh'
  ];

  constructor(
    private readonly http: HttpClient,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {
    const currentYear = new Date().getFullYear();
    for (let y = currentYear; y >= currentYear - 100; y--) {
      this.years.push(String(y));
    }
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEdit.set(true);
        this.customerId.set(id);
        this.loadCustomer(id);
      }
    });
  }

  ngOnDestroy(): void {
    this.stopWebcam();
  }

  private emptyForm(): CustomerFormData {
    return {
      code: '',
      partyType: 'Customer',
      hos: 'HOS',
      accountGroup: 'Sundry Debtors',
      title: 'Ms.',
      firstName: '',
      lastName: '',
      relationType: 'S/o',
      fatherName: '',
      motherName: '',
      gender: 'F',
      dobDay: '',
      dobMonth: '',
      dobYear: '',
      spouseName: '',
      spouseDobDay: '',
      spouseDobMonth: '',
      spouseDobYear: '',
      caste: '',
      qualification: '',
      maritalStatus: '',
      annivDay: '',
      annivMonth: '',
      annivYear: '',
      panNo: '',
      gstin: '',
      taxNo: '',
      aadhaarNo: '',
      bankName: '',
      bankAccName: '',
      bankAccNo: '',
      bankIfscCode: '',
      occupation: '',
      annualIncome: '',
      nomineeName: '',
      nomineeRelation: '',
      designation: '',
      loginId: '',
      portalPassword: '',
      masterPassword: '',
      businessName: '',
      officeAddress: '',
      permanentAddress: '',
      currentAddress: '',
      shippingAddress: '',
      village: '',
      wardNumber: '',
      tehsil: '',
      city: '',
      countryName: 'India',
      pincode: '',
      stateName: '',
      otherInfo: '',
      phone: '',
      email: '',
      photoBase64: '',
      aadhaarFrontBase64: '',
      aadhaarFrontDesc: '',
      aadhaarBackBase64: '',
      aadhaarBackDesc: '',
      sendWelcomeSms: true
    };
  }

  private loadCustomer(id: string): void {
    this.http.get<any>(`${environment.apiBaseUrl}/masters/customers/${id}`).subscribe({
      next: (data) => {
        const empty = this.emptyForm();
        const merged: any = { ...empty };
        for (const k of Object.keys(empty)) {
          if (data[k] !== undefined && data[k] !== null) {
            merged[k] = data[k];
          }
        }
        merged.gender = merged.gender || 'F';
        merged.title = merged.title || 'Ms.';
        merged.hos = merged.hos || 'HOS';
        merged.accountGroup = merged.accountGroup || 'Sundry Debtors';
        merged.countryName = merged.countryName || 'India';
        merged.relationType = merged.relationType || 'S/o';
        merged.partyType = data.partyType || merged.partyType || 'Customer';

        this.form = merged;
        this.parseDates(data);
      },
      error: () => {
        this.message.set({ text: 'Could not load customer data', type: 'error' });
      }
    });
  }

  private parseDates(data: any): void {
    if (data.dob && data.dob.includes('-')) {
      const parts = data.dob.split('-');
      if (parts.length === 3) {
        this.form.dobYear = parts[0];
        this.form.dobMonth = parts[1];
        this.form.dobDay = parts[2];
      }
    }
    if (data.spouseDob && data.spouseDob.includes('-')) {
      const parts = data.spouseDob.split('-');
      if (parts.length === 3) {
        this.form.spouseDobYear = parts[0];
        this.form.spouseDobMonth = parts[1];
        this.form.spouseDobDay = parts[2];
      }
    }
    if (data.anniversaryDate && data.anniversaryDate.includes('-')) {
      const parts = data.anniversaryDate.split('-');
      if (parts.length === 3) {
        this.form.annivYear = parts[0];
        this.form.annivMonth = parts[1];
        this.form.annivDay = parts[2];
      }
    }
  }

  private formatDates(): { dob: string; spouseDob: string; anniversaryDate: string } {
    const dob = this.form.dobYear && this.form.dobMonth && this.form.dobDay
      ? `${this.form.dobYear}-${this.form.dobMonth}-${this.form.dobDay}`
      : '';
    const spouseDob = this.form.spouseDobYear && this.form.spouseDobMonth && this.form.spouseDobDay
      ? `${this.form.spouseDobYear}-${this.form.spouseDobMonth}-${this.form.spouseDobDay}`
      : '';
    const anniversaryDate = this.form.annivYear && this.form.annivMonth && this.form.annivDay
      ? `${this.form.annivYear}-${this.form.annivMonth}-${this.form.annivDay}`
      : '';
    return { dob, spouseDob, anniversaryDate };
  }

  // File Upload Handlers
  onFileSelected(event: Event, target: 'photo' | 'aadhaarFront' | 'aadhaarBack'): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      const reader = new FileReader();
      reader.onload = () => {
        const base64 = reader.result as string;
        if (target === 'photo') this.form.photoBase64 = base64;
        else if (target === 'aadhaarFront') this.form.aadhaarFrontBase64 = base64;
        else if (target === 'aadhaarBack') this.form.aadhaarBackBase64 = base64;
      };
      reader.readAsDataURL(file);
    }
  }

  // Webcam Handlers
  openWebcam(target: 'photo' | 'aadhaarFront' | 'aadhaarBack'): void {
    this.activeWebcamTarget = target;
    this.webcamOpen.set(true);
    navigator.mediaDevices?.getUserMedia({ video: { facingMode: 'user', width: 640, height: 480 } })
      .then(stream => {
        this.mediaStream = stream;
        setTimeout(() => {
          if (this.videoElement) {
            this.videoElement.nativeElement.srcObject = stream;
          }
        }, 100);
      })
      .catch(err => {
        this.message.set({ text: 'Webcam not accessible or permission denied. Please use "Computer" upload instead.', type: 'error' });
        this.webcamOpen.set(false);
      });
  }

  captureWebcam(): void {
    if (!this.videoElement?.nativeElement) return;
    const video = this.videoElement.nativeElement;
    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;
    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
      const dataUrl = canvas.toDataURL('image/jpeg', 0.85);
      if (this.activeWebcamTarget === 'photo') this.form.photoBase64 = dataUrl;
      else if (this.activeWebcamTarget === 'aadhaarFront') this.form.aadhaarFrontBase64 = dataUrl;
      else if (this.activeWebcamTarget === 'aadhaarBack') this.form.aadhaarBackBase64 = dataUrl;
    }
    this.stopWebcam();
  }

  stopWebcam(): void {
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach(track => track.stop());
      this.mediaStream = null;
    }
    this.webcamOpen.set(false);
    this.activeWebcamTarget = null;
  }

  isRequired(key: CustomerFieldKey): boolean {
    return this.validationConfig.isRequired('full', key);
  }

  placeholder(key: CustomerFieldKey): string {
    return this.validationConfig.placeholder('full', key);
  }

  fieldError(key: CustomerFieldKey): string | null {
    return this.fieldErrors()[key] || null;
  }

  clearFieldError(key: CustomerFieldKey): void {
    const current = this.fieldErrors();
    if (!current[key]) return;
    const next = { ...current };
    delete next[key];
    this.fieldErrors.set(next);
  }

  onFieldBlur(key: CustomerFieldKey): void {
    const rules = this.validationConfig.getRules('full');
    const value = (this.form as any)[key] as string;
    const msg = validateCustomerField(key, value, rules, this.form, 'full');
    const next = { ...this.fieldErrors() };
    if (msg) {
      next[key] = msg;
    } else {
      delete next[key];
    }
    this.fieldErrors.set(next);
  }

  verifyAadhaar(): void {
    const raw = normalizeAadhaar(this.form.aadhaarNo);
    if (!raw) {
      this.aadhaarValidationMsg.set('Please enter an Aadhaar number to verify.');
      return;
    }
    if (isFormatValid('aadhaar', raw)) {
      this.aadhaarValidationMsg.set('✓ Aadhaar format is valid (12-digit UIDAI standard).');
      this.clearFieldError('aadhaarNo');
    } else {
      this.aadhaarValidationMsg.set('⚠ Invalid Aadhaar number. Must be 12 digits starting with 2–9.');
    }
  }

  saveCustomer(): void {
    if (this.isSaving()) return;

    const str = (v: any): string => (typeof v === 'string' ? v.trim() : (v != null ? String(v).trim() : ''));
    const strOrUndef = (v: any): string | undefined => {
      const s = str(v);
      return s ? s : undefined;
    };
    const upperOrUndef = (v: any): string | undefined => {
      const s = str(v);
      return s ? s.toUpperCase() : undefined;
    };

    const rules = this.validationConfig.getRules('full');
    const result = validateCustomer(
      {
        firstName: str(this.form.firstName),
        lastName: str(this.form.lastName),
        businessName: str(this.form.businessName),
        phone: str(this.form.phone),
        email: str(this.form.email),
        pincode: str(this.form.pincode),
        panNo: str(this.form.panNo),
        gstin: str(this.form.gstin),
        aadhaarNo: str(this.form.aadhaarNo),
        bankIfscCode: str(this.form.bankIfscCode),
        bankAccNo: str(this.form.bankAccNo)
      },
      rules,
      'full'
    );

    if (!result.valid) {
      this.fieldErrors.set(result.errors);
      this.message.set({ text: result.summary || 'Please fix the highlighted fields', type: 'error' });
      return;
    }

    const first = str(this.form.firstName);
    const last = str(this.form.lastName);
    const biz = str(this.form.businessName);
    if (!first && !last && !biz) {
      this.message.set({ text: 'Please provide at least a First Name or Business Name.', type: 'error' });
      return;
    }

    this.fieldErrors.set({});
    this.isSaving.set(true);
    const dates = this.formatDates();
    const normalized = normalizeCustomerForSave({
      firstName: str(this.form.firstName),
      lastName: str(this.form.lastName),
      businessName: str(this.form.businessName),
      phone: str(this.form.phone),
      email: str(this.form.email),
      pincode: str(this.form.pincode),
      panNo: str(this.form.panNo),
      gstin: str(this.form.gstin),
      aadhaarNo: str(this.form.aadhaarNo),
      bankIfscCode: str(this.form.bankIfscCode),
      bankAccNo: str(this.form.bankAccNo)
    });

    const fullName = [normalized.firstName || first, last].filter(Boolean).join(' ').trim()
      || normalized.businessName
      || biz;

    const payload: any = {
      code: strOrUndef(this.form.code),
      name: fullName,
      firstName: normalized.firstName || strOrUndef(this.form.firstName),
      lastName: strOrUndef(this.form.lastName),
      partyType: this.form.partyType || 'Customer',
      title: this.form.title || 'Ms.',
      gender: this.form.gender || 'F',
      relationType: this.form.relationType || 'S/o',
      fatherName: strOrUndef(this.form.fatherName),
      motherName: strOrUndef(this.form.motherName),
      spouseName: strOrUndef(this.form.spouseName),
      caste: strOrUndef(this.form.caste),
      qualification: strOrUndef(this.form.qualification),
      maritalStatus: strOrUndef(this.form.maritalStatus),
      dob: dates.dob || undefined,
      spouseDob: dates.spouseDob || undefined,
      anniversaryDate: dates.anniversaryDate || undefined,
      hos: this.form.hos || 'HOS',
      accountGroup: this.form.accountGroup || 'Sundry Debtors',
      panNo: normalized.panNo || upperOrUndef(this.form.panNo),
      gstin: normalized.gstin || upperOrUndef(this.form.gstin),
      taxNo: strOrUndef(this.form.taxNo),
      aadhaarNo: normalized.aadhaarNo || strOrUndef(this.form.aadhaarNo),
      bankName: strOrUndef(this.form.bankName),
      bankAccName: strOrUndef(this.form.bankAccName),
      bankAccNo: normalized.bankAccNo || strOrUndef(this.form.bankAccNo),
      bankIfscCode: normalized.bankIfscCode || upperOrUndef(this.form.bankIfscCode),
      occupation: strOrUndef(this.form.occupation),
      annualIncome: strOrUndef(this.form.annualIncome),
      nomineeName: strOrUndef(this.form.nomineeName),
      nomineeRelation: strOrUndef(this.form.nomineeRelation),
      designation: strOrUndef(this.form.designation),
      loginId: strOrUndef(this.form.loginId),
      portalPassword: strOrUndef(this.form.portalPassword),
      masterPassword: strOrUndef(this.form.masterPassword),
      businessName: normalized.businessName || strOrUndef(this.form.businessName),
      officeAddress: strOrUndef(this.form.officeAddress),
      permanentAddress: strOrUndef(this.form.permanentAddress),
      currentAddress: strOrUndef(this.form.currentAddress),
      shippingAddress: strOrUndef(this.form.shippingAddress),
      village: strOrUndef(this.form.village),
      wardNumber: strOrUndef(this.form.wardNumber),
      tehsil: strOrUndef(this.form.tehsil),
      city: strOrUndef(this.form.city),
      countryName: strOrUndef(this.form.countryName) || 'India',
      pincode: normalized.pincode || strOrUndef(this.form.pincode),
      stateName: strOrUndef(this.form.stateName),
      otherInfo: strOrUndef(this.form.otherInfo),
      phone: normalized.phone || strOrUndef(this.form.phone),
      email: normalized.email || strOrUndef(this.form.email),
      address: strOrUndef(this.form.currentAddress) || strOrUndef(this.form.officeAddress) || undefined,
      photoBase64: this.form.photoBase64 || undefined,
      aadhaarFrontBase64: this.form.aadhaarFrontBase64 || undefined,
      aadhaarFrontDesc: strOrUndef(this.form.aadhaarFrontDesc),
      aadhaarBackBase64: this.form.aadhaarBackBase64 || undefined,
      aadhaarBackDesc: strOrUndef(this.form.aadhaarBackDesc),
      sendWelcomeSms: !!this.form.sendWelcomeSms
    };

    const req$ = this.isEdit() && this.customerId()
      ? this.http.put(`${environment.apiBaseUrl}/masters/customers/${this.customerId()}`, payload)
      : this.http.post(`${environment.apiBaseUrl}/masters/customers`, payload);

    req$.subscribe({
      next: (res: any) => {
        this.isSaving.set(false);
        this.message.set({
          text: `Customer ${this.isEdit() ? 'updated' : 'created'} successfully! Code: ${res.code || 'Assigned'}`,
          type: 'success'
        });
        setTimeout(() => {
          this.router.navigate(['/app/masters'], { queryParams: { tab: 'customer' } });
        }, 1200);
      },
      error: (err: any) => {
        this.isSaving.set(false);
        let errMsg = err?.error?.message || 'Failed to save customer. Please check required fields.';
        if (err?.error?.fieldErrors && err.error.fieldErrors.length > 0) {
          const details = err.error.fieldErrors.map((f: any) => `${f.field}: ${f.message}`).join(', ');
          errMsg += ` (${details})`;
        }
        this.message.set({ text: errMsg, type: 'error' });
      }
    });
  }

  resetForm(): void {
    this.form = this.emptyForm();
    this.message.set(null);
    this.aadhaarValidationMsg.set(null);
    this.fieldErrors.set({});
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

