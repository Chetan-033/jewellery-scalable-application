import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  inject,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CustomerFieldKey } from '../../core/customer-validation/customer-field-rules';
import { CustomerValidationConfigService } from '../../core/customer-validation/customer-validation-config.service';
import {
  FieldErrorMap,
  normalizeCustomerForSave,
  validateCustomer,
  validateCustomerField
} from '../../core/customer-validation/customer-validators';
import { EnterNextFieldDirective } from '../enter-next-field.directive';

export interface QuickCustomerPayload {
  title: string;
  gender: string;
  firstName: string;
  lastName: string;
  spouseName: string;
  phone: string;
  altPhone: string;
  address: string;
  countryName: string;
  stateName: string;
  city: string;
  village: string;
  pincode: string;
  gstin: string;
  panNo: string;
  aadhaarNo: string;
  email: string;
  partyType: string;
}

export interface SavedCustomer {
  id: string;
  code?: string;
  name?: string;
  phone?: string;
}

export type QuickAddContinueAction = 'loan' | 'sale' | 'udhaar' | 'none';

@Component({
  selector: 'app-quick-add-customer-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, EnterNextFieldDirective],
  templateUrl: './quick-add-customer-modal.component.html',
  styleUrl: './quick-add-customer-modal.component.scss'
})
export class QuickAddCustomerModalComponent implements OnChanges, OnDestroy {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly validationConfig = inject(CustomerValidationConfigService);

  @Input({ required: true }) open = false;
  /** When set, plain Save also continues to this destination after create. */
  @Input() continueAction: QuickAddContinueAction = 'none';

  @Output() readonly closed = new EventEmitter<void>();
  @Output() readonly saved = new EventEmitter<SavedCustomer>();

  readonly message = signal<string | null>(null);
  readonly saving = signal(false);
  readonly fieldErrors = signal<FieldErrorMap>({});

  cust: QuickCustomerPayload = this.emptyCustomer();

  private saveSub: Subscription | null = null;
  private closeTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['open']?.currentValue === true && !changes['open'].previousValue) {
      this.resetFormState();
    }
  }

  ngOnDestroy(): void {
    this.saveSub?.unsubscribe();
    if (this.closeTimer) {
      clearTimeout(this.closeTimer);
      this.closeTimer = null;
    }
  }

  isRequired(key: CustomerFieldKey): boolean {
    return this.validationConfig.isRequired('quick', key);
  }

  placeholder(key: CustomerFieldKey): string {
    return this.validationConfig.placeholder('quick', key);
  }

  fieldError(key: CustomerFieldKey): string | null {
    return this.fieldErrors()[key] || null;
  }

  hasFieldErrors(): boolean {
    return Object.keys(this.fieldErrors()).length > 0;
  }

  clearFieldError(key: CustomerFieldKey): void {
    const current = this.fieldErrors();
    if (!current[key]) return;
    const next = { ...current };
    delete next[key];
    this.fieldErrors.set(next);
  }

  onFieldBlur(key: CustomerFieldKey): void {
    const rules = this.validationConfig.getRules('quick');
    const msg = validateCustomerField(
      key,
      this.cust[key as keyof QuickCustomerPayload] as string,
      rules,
      this.cust,
      'quick'
    );
    const next = { ...this.fieldErrors() };
    if (msg) {
      next[key] = msg;
    } else {
      delete next[key];
    }
    this.fieldErrors.set(next);
  }

  close(): void {
    if (this.saving()) return;
    this.closed.emit();
  }

  onBackdropClick(): void {
    this.close();
  }

  saveCustomer(): void {
    this.persistAndContinue(this.continueAction);
  }

  saveAndGoLoan(): void {
    this.persistAndContinue('loan');
  }

  saveAndGoSale(): void {
    this.persistAndContinue('sale');
  }

  saveAndGoUdhaar(): void {
    this.persistAndContinue('udhaar');
  }

  private persistAndContinue(action: QuickAddContinueAction): void {
    if (this.saving()) return;

    const rules = this.validationConfig.getRules('quick');
    const result = validateCustomer(this.cust, rules, 'quick');

    if (!result.valid) {
      this.fieldErrors.set(result.errors);
      this.message.set(result.summary);
      return;
    }

    this.fieldErrors.set({});
    this.saving.set(true);
    this.message.set(null);

    const normalized = normalizeCustomerForSave({ ...this.cust });
    const name =
      [normalized.firstName, this.cust.lastName].filter((v) => !!String(v || '').trim()).join(' ').trim() ||
      undefined;

    const payload = {
      title: this.cust.title,
      gender: this.cust.gender,
      firstName: normalized.firstName,
      lastName: this.cust.lastName.trim() || undefined,
      spouseName: this.cust.spouseName.trim() || undefined,
      phone: normalized.phone,
      altPhone: normalized.altPhone,
      address: this.cust.address.trim() || undefined,
      countryName: this.cust.countryName.trim() || 'India',
      stateName: this.cust.stateName.trim() || undefined,
      city: this.cust.city.trim() || undefined,
      village: this.cust.village.trim() || undefined,
      pincode: normalized.pincode,
      gstin: normalized.gstin,
      panNo: normalized.panNo,
      aadhaarNo: normalized.aadhaarNo,
      email: normalized.email,
      partyType: this.cust.partyType || 'CUSTOMER',
      name
    };

    this.saveSub?.unsubscribe();
    this.saveSub = this.http.post<SavedCustomer>(`${environment.apiBaseUrl}/masters/customers`, payload).subscribe({
      next: (created) => {
        this.saving.set(false);
        this.message.set('Customer saved');
        this.cust = this.emptyCustomer();
        this.fieldErrors.set({});
        this.saved.emit(created);
        this.navigateAfterSave(created, action);
      },
      error: (err) => {
        this.saving.set(false);
        this.message.set(err?.error?.message || 'Customer save failed');
      }
    });
  }

  private navigateAfterSave(created: SavedCustomer, action: QuickAddContinueAction): void {
    if (!created?.id) {
      this.closed.emit();
      return;
    }

    if (action === 'loan') {
      this.closed.emit();
      void this.router.navigate(['/app/customers', created.id], {
        queryParams: { action: 'add-loan' }
      });
      return;
    }

    if (action === 'sale') {
      this.closed.emit();
      void this.router.navigate(['/app/sales'], {
        queryParams: { customerId: created.id }
      });
      return;
    }

    if (action === 'udhaar') {
      this.closed.emit();
      void this.router.navigate(['/app/credit'], {
        queryParams: { customerId: created.id }
      });
      return;
    }

    if (this.closeTimer) clearTimeout(this.closeTimer);
    this.closeTimer = setTimeout(() => {
      this.closeTimer = null;
      this.closed.emit();
    }, 600);
  }

  private resetFormState(): void {
    this.saveSub?.unsubscribe();
    this.saveSub = null;
    if (this.closeTimer) {
      clearTimeout(this.closeTimer);
      this.closeTimer = null;
    }
    this.cust = this.emptyCustomer();
    this.fieldErrors.set({});
    this.message.set(null);
    this.saving.set(false);
  }

  private emptyCustomer(): QuickCustomerPayload {
    return {
      title: 'Mr.',
      gender: 'M',
      firstName: '',
      lastName: '',
      spouseName: '',
      phone: '',
      altPhone: '',
      address: '',
      countryName: 'India',
      stateName: '',
      city: '',
      village: '',
      pincode: '',
      gstin: '',
      panNo: '',
      aadhaarNo: '',
      email: '',
      partyType: 'CUSTOMER'
    };
  }
}
