import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FirebaseService } from '../core/firebase.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
    standalone: false
})
export class LoginComponent {
  loading = false;
  error: string | null = null;

  constructor(
    private readonly firebase: FirebaseService,
    private readonly router: Router,
  ) {}

  async signIn(): Promise<void> {
    this.loading = true;
    this.error = null;
    try {
      await this.firebase.signInWithGoogle();
      await this.router.navigateByUrl('/dashboard');
    } catch (err: unknown) {
      this.error = err instanceof Error ? err.message : 'Sign-in failed';
    } finally {
      this.loading = false;
    }
  }
}
