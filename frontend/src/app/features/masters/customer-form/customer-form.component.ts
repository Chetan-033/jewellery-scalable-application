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
        this.form = {
          ...this.emptyForm(),
          ...data,
          // Never hydrate passwords from API (backend omits them; blank = leave unchanged on save)
          portalPassword: '',
          masterPassword: '',
          gender: data.gender || 'F',
          title: data.title || 'Ms.',
          hos: data.hos || 'HOS',
          accountGroup: data.accountGroup || 'Sundry Debtors',
          countryName: data.countryName || 'India'
        };
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

    const rules = this.validationConfig.getRules('full');
    const result = validateCustomer(
      {
        firstName: this.form.firstName,
        lastName: this.form.lastName,
        businessName: this.form.businessName,
        phone: this.form.phone,
        email: this.form.email,
        pincode: this.form.pincode,
        panNo: this.form.panNo,
        gstin: this.form.gstin,
        aadhaarNo: this.form.aadhaarNo,
        bankIfscCode: this.form.bankIfscCode,
        bankAccNo: this.form.bankAccNo
      },
      rules,
      'full'
    );

    if (!result.valid) {
      this.fieldErrors.set(result.errors);
      this.message.set({ text: result.summary || 'Please fix the highlighted fields', type: 'error' });
      return;
    }

    this.fieldErrors.set({});
    this.isSaving.set(true);
    const dates = this.formatDates();
    const normalized = normalizeCustomerForSave({
      firstName: this.form.firstName,
      lastName: this.form.lastName,
      businessName: this.form.businessName,
      phone: this.form.phone,
      email: this.form.email,
      pincode: this.form.pincode,
      panNo: this.form.panNo,
      gstin: this.form.gstin,
      aadhaarNo: this.form.aadhaarNo,
      bankIfscCode: this.form.bankIfscCode,
      bankAccNo: this.form.bankAccNo
    });

    const fullName = [normalized.firstName, this.form.lastName.trim()].filter(Boolean).join(' ').trim()
      || normalized.businessName
      || this.form.businessName.trim();

    const payload: any = {
      code: this.form.code.trim() || undefined,
      name: fullName,
      firstName: normalized.firstName || undefined,
      lastName: this.form.lastName.trim() || undefined,
      partyType: this.form.partyType,
      title: this.form.title,
      gender: this.form.gender,
      relationType: this.form.relationType,
      fatherName: this.form.fatherName.trim() || undefined,
      motherName: this.form.motherName.trim() || undefined,
      spouseName: this.form.spouseName.trim() || undefined,
      caste: this.form.caste.trim() || undefined,
      qualification: this.form.qualification.trim() || undefined,
      maritalStatus: this.form.maritalStatus || undefined,
      dob: dates.dob || undefined,
      spouseDob: dates.spouseDob || undefined,
      anniversaryDate: dates.anniversaryDate || undefined,
      hos: this.form.hos,
      accountGroup: this.form.accountGroup,
      panNo: normalized.panNo || undefined,
      gstin: normalized.gstin || undefined,
      taxNo: this.form.taxNo.trim() || undefined,
      aadhaarNo: normalized.aadhaarNo || undefined,
      bankName: this.form.bankName.trim() || undefined,
      bankAccName: this.form.bankAccName.trim() || undefined,
      bankAccNo: normalized.bankAccNo || undefined,
      bankIfscCode: normalized.bankIfscCode || undefined,
      occupation: this.form.occupation.trim() || undefined,
      annualIncome: this.form.annualIncome.trim() || undefined,
      nomineeName: this.form.nomineeName.trim() || undefined,
      nomineeRelation: this.form.nomineeRelation.trim() || undefined,
      designation: this.form.designation.trim() || undefined,
      loginId: this.form.loginId.trim() || undefined,
      portalPassword: this.form.portalPassword.trim() || undefined,
      masterPassword: this.form.masterPassword.trim() || undefined,
      businessName: normalized.businessName || this.form.businessName.trim() || undefined,
      officeAddress: this.form.officeAddress.trim() || undefined,
      permanentAddress: this.form.permanentAddress.trim() || undefined,
      currentAddress: this.form.currentAddress.trim() || undefined,
      shippingAddress: this.form.shippingAddress.trim() || undefined,
      village: this.form.village.trim() || undefined,
      wardNumber: this.form.wardNumber.trim() || undefined,
      tehsil: this.form.tehsil.trim() || undefined,
      city: this.form.city.trim() || undefined,
      countryName: this.form.countryName.trim() || 'India',
      pincode: normalized.pincode || undefined,
      stateName: this.form.stateName.trim() || undefined,
      otherInfo: this.form.otherInfo.trim() || undefined,
      phone: normalized.phone || undefined,
      email: normalized.email || undefined,
      address: this.form.currentAddress.trim() || this.form.officeAddress.trim() || undefined,
      photoBase64: this.form.photoBase64 || undefined,
      aadhaarFrontBase64: this.form.aadhaarFrontBase64 || undefined,
      aadhaarFrontDesc: this.form.aadhaarFrontDesc.trim() || undefined,
      aadhaarBackBase64: this.form.aadhaarBackBase64 || undefined,
      aadhaarBackDesc: this.form.aadhaarBackDesc.trim() || undefined,
      sendWelcomeSms: this.form.sendWelcomeSms
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
          this.router.navigate(['/app/masters'], { queryParams: { tab: 'customers' } });
        }, 1500);
      },
      error: (err) => {
        this.isSaving.set(false);
        const errMsg = err?.error?.message || 'Failed to save customer. Please check required fields.';
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

