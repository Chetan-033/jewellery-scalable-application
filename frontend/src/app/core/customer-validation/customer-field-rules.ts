/**
 * Customer field validation rules.
 *
 * Current: hard-coded defaults for quick / full forms.
 * Future: load overrides from tenant settings so shops can mark fields
 * required / optional without code changes.
 */

export type CustomerFormProfile = 'quick' | 'full';

export type CustomerFieldFormat =
  | 'mobile'
  | 'email'
  | 'pan'
  | 'gstin'
  | 'aadhaar'
  | 'pincode'
  | 'ifsc'
  | 'bankAccount';

export type CustomerFieldKey =
  | 'firstName'
  | 'lastName'
  | 'businessName'
  | 'phone'
  | 'altPhone'
  | 'email'
  | 'pincode'
  | 'panNo'
  | 'gstin'
  | 'aadhaarNo'
  | 'bankIfscCode'
  | 'bankAccNo'
  | 'address'
  | 'city'
  | 'stateName';

export interface CustomerFieldRule {
  key: CustomerFieldKey;
  label: string;
  /** Soft required — empty value blocks save when true. */
  required: boolean;
  /** Format check only when value is present (or required). */
  format?: CustomerFieldFormat;
  /** Input placeholder hint (format example). */
  placeholder?: string;
}

/** Default rules — future settings API can override `required` per field. */
export const DEFAULT_CUSTOMER_FIELD_RULES: Record<CustomerFormProfile, CustomerFieldRule[]> = {
  quick: [
    { key: 'firstName', label: 'First name', required: true, placeholder: 'e.g. Ramesh' },
    { key: 'phone', label: 'Mobile', required: true, format: 'mobile', placeholder: '10-digit e.g. 9876543210' },
    { key: 'altPhone', label: 'Phone', required: false, format: 'mobile', placeholder: 'Optional 10-digit' },
    { key: 'email', label: 'Email', required: false, format: 'email', placeholder: 'name@email.com' },
    { key: 'pincode', label: 'Pincode', required: false, format: 'pincode', placeholder: '6-digit e.g. 302001' },
    { key: 'panNo', label: 'PAN', required: false, format: 'pan', placeholder: 'ABCDE1234F' },
    { key: 'gstin', label: 'GSTIN', required: false, format: 'gstin', placeholder: '15-char GSTIN' },
    { key: 'aadhaarNo', label: 'Aadhaar', required: false, format: 'aadhaar', placeholder: '12-digit Aadhaar' }
  ],
  full: [
    // Name: either firstName or businessName must be present (special rule in validator)
    { key: 'firstName', label: 'First name', required: false, placeholder: 'First Name *' },
    { key: 'businessName', label: 'Business name', required: false, placeholder: 'SHOP / BUSINESS NAME' },
    { key: 'phone', label: 'Mobile', required: true, format: 'mobile', placeholder: '10-digit mobile * e.g. 9876543210' },
    { key: 'email', label: 'Email', required: false, format: 'email', placeholder: 'name@email.com' },
    { key: 'pincode', label: 'Pincode', required: false, format: 'pincode', placeholder: '6-digit PIN e.g. 302001' },
    { key: 'panNo', label: 'PAN', required: false, format: 'pan', placeholder: 'PAN e.g. ABCDE1234F' },
    { key: 'gstin', label: 'GSTIN', required: false, format: 'gstin', placeholder: '15-char GSTIN' },
    { key: 'aadhaarNo', label: 'Aadhaar', required: false, format: 'aadhaar', placeholder: '12-digit Aadhaar' },
    { key: 'bankIfscCode', label: 'IFSC', required: false, format: 'ifsc', placeholder: 'IFSC e.g. SBIN0001234' },
    { key: 'bankAccNo', label: 'Bank account no', required: false, format: 'bankAccount', placeholder: '9–18 digit A/C no' }
  ]
};

export interface CustomerFieldRuleOverride {
  key: CustomerFieldKey;
  required?: boolean;
}
