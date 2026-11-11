import {
  CustomerFieldFormat,
  CustomerFieldKey,
  CustomerFieldRule,
  CustomerFormProfile
} from './customer-field-rules';

export type CustomerValues = Partial<Record<CustomerFieldKey, string | null | undefined>> & {
  lastName?: string | null;
};

export type FieldErrorMap = Partial<Record<CustomerFieldKey, string>>;

export interface CustomerValidationResult {
  valid: boolean;
  errors: FieldErrorMap;
  /** First error message for flash / banner. */
  summary: string | null;
}

const MOBILE_RE = /^[6-9]\d{9}$/;
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/i;
const PAN_RE = /^[A-Z]{5}\d{4}[A-Z]$/;
const GSTIN_RE = /^\d{2}[A-Z]{5}\d{4}[A-Z][1-9A-Z]Z[0-9A-Z]$/;
const AADHAAR_RE = /^[2-9]\d{11}$/;
const PINCODE_RE = /^[1-9]\d{5}$/;
const IFSC_RE = /^[A-Z]{4}0[A-Z0-9]{6}$/;
const BANK_ACC_RE = /^\d{9,18}$/;

export function digitsOnly(value: string): string {
  return (value || '').replace(/\D/g, '');
}

/** Normalize Indian mobile: strip +91 / 91 / 0 / spaces. */
export function normalizeMobile(value: string | null | undefined): string {
  let raw = digitsOnly(value || '');
  if (raw.length === 12 && raw.startsWith('91')) {
    raw = raw.slice(2);
  } else if (raw.length === 11 && raw.startsWith('0')) {
    raw = raw.slice(1);
  }
  return raw;
}

export function normalizePan(value: string | null | undefined): string {
  return (value || '').replace(/\s+/g, '').toUpperCase();
}

export function normalizeGstin(value: string | null | undefined): string {
  return (value || '').replace(/\s+/g, '').toUpperCase();
}

export function normalizeAadhaar(value: string | null | undefined): string {
  return digitsOnly(value || '');
}

export function normalizeIfsc(value: string | null | undefined): string {
  return (value || '').replace(/\s+/g, '').toUpperCase();
}

export function formatError(format: CustomerFieldFormat, label: string): string {
  switch (format) {
    case 'mobile':
      return `${label} must be a valid 10-digit Indian mobile (starts with 6–9)`;
    case 'email':
      return `Enter a valid ${label.toLowerCase()}`;
    case 'pan':
      return `${label} must be like ABCDE1234F`;
    case 'gstin':
      return `${label} must be a valid 15-character GSTIN`;
    case 'aadhaar':
      return `${label} must be a valid 12-digit Aadhaar`;
    case 'pincode':
      return `${label} must be a 6-digit Indian PIN`;
    case 'ifsc':
      return `${label} must be like SBIN0001234`;
    case 'bankAccount':
      return `${label} must be 9–18 digits`;
    default:
      return `Invalid ${label}`;
  }
}

export function isFormatValid(format: CustomerFieldFormat, raw: string): boolean {
  switch (format) {
    case 'mobile':
      return MOBILE_RE.test(normalizeMobile(raw));
    case 'email':
      return EMAIL_RE.test(raw.trim());
    case 'pan':
      return PAN_RE.test(normalizePan(raw));
    case 'gstin':
      return GSTIN_RE.test(normalizeGstin(raw));
    case 'aadhaar':
      return AADHAAR_RE.test(normalizeAadhaar(raw));
    case 'pincode':
      return PINCODE_RE.test(digitsOnly(raw));
    case 'ifsc':
      return IFSC_RE.test(normalizeIfsc(raw));
    case 'bankAccount':
      return BANK_ACC_RE.test(digitsOnly(raw));
    default:
      return true;
  }
}

function readValue(values: CustomerValues, key: CustomerFieldKey): string {
  return String(values[key] ?? '').trim();
}

/**
 * Validates customer values against profile rules.
 * Designed so rules can later come from tenant customization.
 */
export function validateCustomer(
  values: CustomerValues,
  rules: CustomerFieldRule[],
  profile: CustomerFormProfile
): CustomerValidationResult {
  const errors: FieldErrorMap = {};

  if (profile === 'full') {
    const first = readValue(values, 'firstName');
    const last = String(values.lastName ?? '').trim();
    const business = readValue(values, 'businessName');
    if (!first && !last && !business) {
      errors.firstName = 'Enter first name or business name';
      errors.businessName = 'Enter first name or business name';
    }
  }

  for (const rule of rules) {
    const raw = readValue(values, rule.key);
    const empty = !raw;

    if (rule.required && empty) {
      // full-form name special-case already handled
      if (profile === 'full' && (rule.key === 'firstName' || rule.key === 'businessName')) {
        continue;
      }
      errors[rule.key] = `${rule.label} is required`;
      continue;
    }

    if (!empty && rule.format && !isFormatValid(rule.format, raw)) {
      errors[rule.key] = formatError(rule.format, rule.label);
    }
  }

  // Cross-field: GSTIN must embed PAN when both provided
  const pan = normalizePan(readValue(values, 'panNo'));
  const gstin = normalizeGstin(readValue(values, 'gstin'));
  if (pan && gstin && PAN_RE.test(pan) && GSTIN_RE.test(gstin)) {
    const embeddedPan = gstin.slice(2, 12);
    if (embeddedPan !== pan) {
      errors.gstin = 'GSTIN does not match the entered PAN';
    }
  }

  const keys = Object.keys(errors) as CustomerFieldKey[];
  let summary: string | null = null;
  if (keys.length === 1) {
    summary = errors[keys[0]] || 'Please fix the highlighted field';
  } else if (keys.length > 1) {
    summary = `Please fix ${keys.length} fields below`;
  }

  return {
    valid: keys.length === 0,
    errors,
    summary
  };
}

/**
 * Blur / live check.
 * Empty values do not show "required" here (that runs on save) so Enter/tab
 * through the form stays quiet. Format is checked only when value is present.
 */
export function validateCustomerField(
  key: CustomerFieldKey,
  value: string | null | undefined,
  rules: CustomerFieldRule[],
  allValues?: CustomerValues,
  _profile: CustomerFormProfile = 'quick'
): string | null {
  const rule = rules.find((r) => r.key === key);
  if (!rule) return null;

  const raw = String(value ?? '').trim();
  if (!raw) {
    return null;
  }

  if (rule.format && !isFormatValid(rule.format, raw)) {
    return formatError(rule.format, rule.label);
  }

  if (key === 'gstin' && allValues) {
    const pan = normalizePan(String(allValues.panNo ?? ''));
    const gstin = normalizeGstin(raw);
    if (pan && GSTIN_RE.test(gstin) && PAN_RE.test(pan)) {
      const embeddedPan = gstin.slice(2, 12);
      if (embeddedPan !== pan) {
        return 'GSTIN does not match the entered PAN';
      }
    }
  }

  return null;
}

/** Normalize identity fields before POST/PUT. Empty strings become undefined. */
export function normalizeCustomerForSave<T extends CustomerValues>(values: T): T {
  const next = { ...values } as T;

  const setOrClear = (key: CustomerFieldKey, val: string | undefined) => {
    (next as CustomerValues)[key] = val && val.length ? val : undefined;
  };

  if (next.phone != null) setOrClear('phone', normalizeMobile(String(next.phone)));
  if (next.altPhone != null) {
    const alt = String(next.altPhone).trim();
    setOrClear('altPhone', alt ? normalizeMobile(alt) : undefined);
  }
  if (next.panNo != null) setOrClear('panNo', normalizePan(String(next.panNo)));
  if (next.gstin != null) setOrClear('gstin', normalizeGstin(String(next.gstin)));
  if (next.aadhaarNo != null) setOrClear('aadhaarNo', normalizeAadhaar(String(next.aadhaarNo)));
  if (next.bankIfscCode != null) setOrClear('bankIfscCode', normalizeIfsc(String(next.bankIfscCode)));
  if (next.pincode != null) setOrClear('pincode', digitsOnly(String(next.pincode)));
  if (next.bankAccNo != null) setOrClear('bankAccNo', digitsOnly(String(next.bankAccNo)));
  if (next.email != null) setOrClear('email', String(next.email).trim());
  if (next.firstName != null) setOrClear('firstName', String(next.firstName).trim());
  if (next.businessName != null) setOrClear('businessName', String(next.businessName).trim());

  return next;
}
