import { Injectable, computed, signal } from '@angular/core';
import {
  CustomerFieldKey,
  CustomerFieldRule,
  CustomerFieldRuleOverride,
  CustomerFormProfile,
  DEFAULT_CUSTOMER_FIELD_RULES
} from './customer-field-rules';

/**
 * Supplies customer validation rules.
 * Today: default rules.
 * Future: merge tenant/shop overrides (which fields are required) from settings API.
 */
@Injectable({ providedIn: 'root' })
export class CustomerValidationConfigService {
  /** Optional shop overrides — keyed by profile. Ready for settings UI later. */
  private readonly overrides = signal<Partial<Record<CustomerFormProfile, CustomerFieldRuleOverride[]>>>({});

  private readonly quickRules = computed(() => this.buildRules('quick'));
  private readonly fullRules = computed(() => this.buildRules('full'));
  private readonly quickMap = computed(() => this.toMap(this.quickRules()));
  private readonly fullMap = computed(() => this.toMap(this.fullRules()));

  getRules(profile: CustomerFormProfile): CustomerFieldRule[] {
    return profile === 'quick' ? this.quickRules() : this.fullRules();
  }

  isRequired(profile: CustomerFormProfile, key: CustomerFieldKey): boolean {
    return !!this.mapFor(profile).get(key)?.required;
  }

  placeholder(profile: CustomerFormProfile, key: CustomerFieldKey): string {
    return this.mapFor(profile).get(key)?.placeholder || '';
  }

  /**
   * Future settings screen can call this (or load from API into this signal).
   * Example: setOverrides('quick', [{ key: 'email', required: true }])
   */
  setOverrides(profile: CustomerFormProfile, overrides: CustomerFieldRuleOverride[]): void {
    this.overrides.update((prev) => ({ ...prev, [profile]: [...overrides] }));
  }

  clearOverrides(profile?: CustomerFormProfile): void {
    if (!profile) {
      this.overrides.set({});
      return;
    }
    this.overrides.update((prev) => {
      const next = { ...prev };
      delete next[profile];
      return next;
    });
  }

  private mapFor(profile: CustomerFormProfile): Map<CustomerFieldKey, CustomerFieldRule> {
    return profile === 'quick' ? this.quickMap() : this.fullMap();
  }

  private toMap(rules: CustomerFieldRule[]): Map<CustomerFieldKey, CustomerFieldRule> {
    return new Map(rules.map((r) => [r.key, r]));
  }

  private buildRules(profile: CustomerFormProfile): CustomerFieldRule[] {
    // Depend on overrides signal so computed caches invalidate correctly.
    const profileOverrides = this.overrides()[profile] || [];
    const base = DEFAULT_CUSTOMER_FIELD_RULES[profile].map((r) => ({ ...r }));
    if (!profileOverrides.length) {
      return base;
    }

    const byKey = new Map(profileOverrides.map((o) => [o.key, o]));
    return base.map((rule) => {
      const ov = byKey.get(rule.key);
      if (!ov || ov.required === undefined) {
        return rule;
      }
      return { ...rule, required: ov.required };
    });
  }
}
