import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FirebaseService } from '../core/firebase.service';
import { environment } from '../../environments/environment';

interface UserResponse {
  id: number;
  firebaseUid: string;
  name: string;
  email: string | null;
  phone: string | null;
  role: string;
  branchId: number | null;
  branchName: string | null;
  active: boolean;
}

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss'],
    standalone: false
})
export class DashboardComponent implements OnInit {
  user: UserResponse | null = null;
  status: 'loading' | 'pending' | 'ready' | 'error' = 'loading';
  errorMessage = '';

  constructor(
    private readonly http: HttpClient,
    private readonly firebase: FirebaseService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.http.get<UserResponse>(`${environment.apiBaseUrl}/auth/me`).subscribe({
      next: (user) => {
        this.user = user;
        this.status = 'ready';
      },
      error: (err) => {
        if (err?.status === 404) {
          this.status = 'pending';
        } else {
          this.status = 'error';
          this.errorMessage = err?.message || 'Failed to load profile';
        }
      },
    });
  }

  async signOut(): Promise<void> {
    await this.firebase.signOut();
    await this.router.navigateByUrl('/login');
  }
}
