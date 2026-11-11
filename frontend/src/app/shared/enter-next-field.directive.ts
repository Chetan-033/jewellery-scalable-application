import { Directive, ElementRef, HostListener } from '@angular/core';

/**
 * On Enter inside an input/select, move focus to the next field in this container.
 * Prevents accidental form submit. Last field focuses the primary save button if present.
 */
@Directive({
  selector: '[appEnterNextField]',
  standalone: true
})
export class EnterNextFieldDirective {
  constructor(private readonly host: ElementRef<HTMLElement>) {}

  @HostListener('keydown.enter', ['$event'])
  onEnter(event: KeyboardEvent): void {
    if (event.defaultPrevented || event.isComposing) return;

    const target = event.target as HTMLElement | null;
    if (!target) return;

    const tag = target.tagName;
    if (tag === 'TEXTAREA' || tag === 'BUTTON' || tag === 'A') return;
    if (!(target instanceof HTMLInputElement || target instanceof HTMLSelectElement)) return;
    if (
      target instanceof HTMLInputElement &&
      (target.type === 'submit' ||
        target.type === 'button' ||
        target.type === 'checkbox' ||
        target.type === 'radio' ||
        target.readOnly)
    ) {
      return;
    }

    event.preventDefault();
    event.stopPropagation();

    const root = this.host.nativeElement;
    const focusables = Array.from(
      root.querySelectorAll<HTMLElement>(
        'input:not([type="hidden"]):not([type="checkbox"]):not([type="radio"]):not([disabled]):not([readonly]), select:not([disabled])'
      )
    ).filter((el) => this.isVisible(el));

    const idx = focusables.indexOf(target);
    if (idx < 0) return;

    if (idx < focusables.length - 1) {
      const next = focusables[idx + 1];
      next.focus();
      if (next instanceof HTMLInputElement && next.type !== 'date' && next.type !== 'file') {
        try {
          next.select();
        } catch {
          /* ignore unsupported select() */
        }
      }
      return;
    }

    const scope =
      root.closest('.modal-card, .customer-form-page, form') || root;
    const saveBtn = scope.querySelector<HTMLButtonElement>('button.btn-primary:not([disabled])');
    saveBtn?.focus();
  }

  private isVisible(el: HTMLElement): boolean {
    return !!(el.offsetWidth || el.offsetHeight || el.getClientRects().length);
  }
}
