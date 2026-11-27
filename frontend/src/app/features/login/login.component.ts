import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  tenantCode = '';
  username = '';
  password = '';
  showPassword = false;
  readonly error = signal<string | null>(null);
  readonly loading = signal(false);
  private returnUrl = '/app/dashboard';

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const next = this.route.snapshot.queryParamMap.get('returnUrl');
    if (next && next.startsWith('/app')) {
      this.returnUrl = next;
    }
  }

  submit(): void {
    const username = this.username.trim();
    const tenantCode = this.tenantCode.trim();
    if (!tenantCode || !username || !this.password) {
      this.error.set('Enter shop code, login ID and password.');
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.auth.login(username, this.password, tenantCode).subscribe({
      next: () => {
        this.loading.set(false);
        this.password = '';
        void this.router.navigateByUrl(this.returnUrl);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(this.describeError(err));
      }
    });
  }

  private describeError(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Cannot reach the API. Start the backend and try again.';
    }
    if (err.status === 401) {
      return 'Invalid shop code, username or password.';
    }
    if (err.status === 422) {
      return err.error?.message || 'Shop is not available.';
    }
    if (err.status >= 500) {
      return 'Server error during sign-in. Please try again.';
    }
    return 'Sign-in failed. Please try again.';
  }
}
