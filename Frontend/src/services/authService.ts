// authService.ts

// Use empty string for relative URLs - Next.js rewrites will proxy to backend
const API_BASE_URL = '';

// Check if dev mode is enabled (mock data, no auth required)
const isDevMode = process.env.NEXT_PUBLIC_DEV_MODE === 'true';

import { mockAuthResponse } from './mockData';
import { decodeJwtPayload } from '@/utils/jwt';

export interface SigninRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
}

export interface AuthenticationResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  token_type: string;
}

export interface VerificationRequest {
  email: string;
  code: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface MessageResponse {
  message: string;
}

export interface AuthConfigResponse {
  resendCooldownSeconds: number;
  resendMaxPerHour: number;
}

class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly REMEMBER_KEY = 'auth_remember';
  private refreshTimeoutId: ReturnType<typeof setTimeout> | null = null;
  private refreshPromise: Promise<AuthenticationResponse> | null = null;

  constructor() {
    if (typeof window !== 'undefined') {
      const token = this.getToken();
      if (token) {
        this.scheduleRefresh(token);
      }
    }
  }

  private scheduleRefresh(token: string): void {
    try {
      if (this.refreshTimeoutId) {
        clearTimeout(this.refreshTimeoutId);
        this.refreshTimeoutId = null;
      }

      const payload = decodeJwtPayload(token);
      if (!payload?.exp) return;

      const expiresAt = payload.exp * 1000;
      const now = Date.now();
      const refreshTime = expiresAt - now - (60 * 1000);

      if (refreshTime > 0) {
        console.debug(`Scheduling token refresh in ${Math.round(refreshTime / 1000)} seconds`);
        this.refreshTimeoutId = setTimeout(() => {
          this.refreshAccessToken().catch(() => {});
        }, refreshTime);
      } else {
        if (this.getRefreshToken()) {
           this.refreshAccessToken().catch(() => {});
        }
      }
    } catch {
      // token refresh scheduling failed silently
    }
  }

  // Store the JWT tokens based on user's "keep me logged in" choice
  setToken(accessToken: string, refreshToken: string, rememberMe: boolean = false): void {
    if (typeof window !== 'undefined') {
      if (rememberMe) {
        // Store in localStorage for persistent login
        localStorage.setItem(this.TOKEN_KEY, accessToken);
        localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
        localStorage.setItem(this.REMEMBER_KEY, 'true');
        // Remove from sessionStorage if it exists there
        sessionStorage.removeItem(this.TOKEN_KEY);
        sessionStorage.removeItem(this.REFRESH_TOKEN_KEY);
      } else {
        // Store in sessionStorage for session-only login
        sessionStorage.setItem(this.TOKEN_KEY, accessToken);
        sessionStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
        localStorage.setItem(this.REMEMBER_KEY, 'false');
        // Remove from localStorage if it exists there
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.REFRESH_TOKEN_KEY);
      }
      
      // Schedule the next refresh
      this.scheduleRefresh(accessToken);
    }
  }

  // Get the JWT token from either localStorage or sessionStorage
  getToken(): string | null {
    if (typeof window !== 'undefined') {
      // Check if user wanted to be remembered
      const rememberMe = localStorage.getItem(this.REMEMBER_KEY) === 'true';
      
      if (rememberMe) {
        // Get token from localStorage
        return localStorage.getItem(this.TOKEN_KEY);
      } else {
        // Get token from sessionStorage
        return sessionStorage.getItem(this.TOKEN_KEY);
      }
    }
    return null;
  }

  // Get the refresh token
  getRefreshToken(): string | null {
    if (typeof window !== 'undefined') {
      const rememberMe = localStorage.getItem(this.REMEMBER_KEY) === 'true';
      if (rememberMe) {
        return localStorage.getItem(this.REFRESH_TOKEN_KEY);
      } else {
        return sessionStorage.getItem(this.REFRESH_TOKEN_KEY);
      }
    }
    return null;
  }

  // Remove the JWT token from storage
  removeToken(): void {
    if (typeof window !== 'undefined') {
      if (this.refreshTimeoutId) {
        clearTimeout(this.refreshTimeoutId);
        this.refreshTimeoutId = null;
      }
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.REFRESH_TOKEN_KEY);
      sessionStorage.removeItem(this.TOKEN_KEY);
      sessionStorage.removeItem(this.REFRESH_TOKEN_KEY);
      localStorage.removeItem(this.REMEMBER_KEY);
    }
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    
    const payload = decodeJwtPayload(token);
    if (!payload) {
      this.removeToken();
      return false;
    }

    const currentTime = Math.floor(Date.now() / 1000);
    return (payload.exp ?? 0) > currentTime;
  }

  // Helper to handle response errors
  private async handleResponseError(response: Response): Promise<never> {
    const text = await response.text();
    let errorData: unknown;
    try {
      errorData = JSON.parse(text);
    } catch {
      errorData = text;
    }

    const possibleMessage =
      typeof errorData === 'object' && errorData !== null && 'message' in (errorData as object)
        ? (errorData as { message?: string }).message
        : undefined;

    const message = possibleMessage ?? (typeof errorData === 'string' ? errorData : `HTTP error! status: ${response.status}`);

    throw new Error(message);
  }

  // Sign in user
  async signin(request: SigninRequest, rememberMe: boolean = false): Promise<AuthenticationResponse> {
    if (isDevMode) {
      console.debug('[DEV MODE] Returning mock auth response');
      this.setToken(mockAuthResponse.access_token, mockAuthResponse.refresh_token, rememberMe);
      return mockAuthResponse;
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/gatekeeper/auth/sign-in`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      await this.handleResponseError(response);
    }

    const data: AuthenticationResponse = await response.json();
    this.setToken(data.access_token, data.refresh_token, rememberMe);
    return data;
  }

  // Sign up user
  async signup(request: SignupRequest): Promise<MessageResponse> {
    if (isDevMode) {
      console.debug('[DEV MODE] Returning mock signup response');
      return { message: 'User registered successfully (dev mode)' };
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/gatekeeper/auth/sign-up`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      await this.handleResponseError(response);
    }

    return await response.json();
  }

  // Verify email
  async verify(request: VerificationRequest): Promise<MessageResponse> {
    if (isDevMode) {
      console.debug('[DEV MODE] Returning mock verify response');
      return { message: 'Email verified successfully (dev mode)' };
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/gatekeeper/auth/verify`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      await this.handleResponseError(response);
    }

    return await response.json();
  }

  // Resend verification email
  async resendVerification(email: string): Promise<MessageResponse> {
    if (isDevMode) {
      console.debug('[DEV MODE] Returning mock resend verification response');
      return { message: 'Verification email sent (dev mode)' };
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/gatekeeper/auth/resend-verification?email=${encodeURIComponent(email)}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      await this.handleResponseError(response);
    }

    return await response.json();
  }

  // Refresh access token
  async refreshAccessToken(): Promise<AuthenticationResponse> {
    // In dev mode, return mock response
    if (isDevMode) {
      console.debug('[DEV MODE] Returning mock refresh token response');
      const rememberMe = localStorage.getItem(this.REMEMBER_KEY) === 'true';
      this.setToken(mockAuthResponse.access_token, mockAuthResponse.refresh_token, rememberMe);
      return mockAuthResponse;
    }

    if (this.refreshPromise) {
      return this.refreshPromise;
    }

    this.refreshPromise = (async () => {
      try {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        const response = await fetch(`${API_BASE_URL}/api/v1/gatekeeper/auth/refresh`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ refreshToken }),
        });

        if (!response.ok) {
          this.removeToken();
          await this.handleResponseError(response);
        }

        const data: AuthenticationResponse = await response.json();
        const rememberMe = localStorage.getItem(this.REMEMBER_KEY) === 'true';
        this.setToken(data.access_token, data.refresh_token, rememberMe);
        return data;
      } finally {
        this.refreshPromise = null;
      }
    })();

    return this.refreshPromise;
  }

  // Logout from current device
  async logout(): Promise<void> {
    try {
      const refreshToken = this.getRefreshToken();
      if (refreshToken) {
        await fetch(`${API_BASE_URL}/api/v1/gatekeeper/auth/logout`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.getToken()}`,
          },
          body: JSON.stringify({ refreshToken }),
        });
      }
    } catch {
      // logout failed silently
    } finally {
      this.removeToken();
    }
  }

  // Logout from all devices
  async logoutAllDevices(): Promise<MessageResponse> {
    if (isDevMode) {
      console.debug('[DEV MODE] Returning mock logout all devices response');
      this.removeToken();
      return { message: 'Logged out from all devices (dev mode)' };
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/gatekeeper/auth/logout-all`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getToken()}`,
      },
    });

    if (!response.ok) {
      await this.handleResponseError(response);
    }

    this.removeToken();
    return await response.json();
  }

  // Fetch auth-related configuration (e.g., resend cooldown)
  async getAuthConfig(): Promise<AuthConfigResponse> {
    // In dev mode, return mock response
    if (isDevMode) {
      console.debug('[DEV MODE] Returning mock auth config response');
      return { resendCooldownSeconds: 60, resendMaxPerHour: 5 };
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/gatekeeper/auth/config`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      await this.handleResponseError(response);
    }

    return await response.json();
  }

  // Request password reset - send code to email
  async requestPasswordReset(email: string): Promise<MessageResponse> {
    // In dev mode, return mock response
    if (isDevMode) {
      console.debug('[DEV MODE] Returning mock password reset request response');
      return { message: 'Password reset code sent (dev mode)' };
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/gatekeeper/auth/password-reset/request`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email }),
    });

    if (!response.ok) {
      await this.handleResponseError(response);
    }

    return await response.json();
  }

  // Verify password reset code
  async verifyPasswordResetCode(email: string, code: string): Promise<MessageResponse> {
    // In dev mode, return mock response
    if (isDevMode) {
      console.debug('[DEV MODE] Returning mock verify password reset code response');
      return { message: 'Password reset code verified (dev mode)' };
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/gatekeeper/auth/password-reset/verify`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, code }),
    });

    if (!response.ok) {
      await this.handleResponseError(response);
    }

    return await response.json();
  }

  // Confirm password reset - set new password
  async confirmPasswordReset(email: string, code: string, password: string): Promise<MessageResponse> {
    // In dev mode, return mock response
    if (isDevMode) {
      console.debug('[DEV MODE] Returning mock confirm password reset response');
      return { message: 'Password reset successfully (dev mode)' };
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/gatekeeper/auth/password-reset/confirm`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, code, password }),
    });

    if (!response.ok) {
      await this.handleResponseError(response);
    }

    return await response.json();
  }
}

const authService = new AuthService();
export default authService;


