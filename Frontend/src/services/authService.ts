// authService.ts

// Use empty string for relative URLs - Next.js rewrites will proxy to backend
const API_BASE_URL = '';

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
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private refreshTimeoutId: any = null;
  private refreshPromise: Promise<AuthenticationResponse> | null = null;

  constructor() {
    if (typeof window !== 'undefined') {
      const token = this.getToken();
      if (token) {
        this.scheduleRefresh(token);
      }
    }
  }

  // Schedule a token refresh before it expires
  private scheduleRefresh(token: string): void {
    try {
      // Clear any existing timeout
      if (this.refreshTimeoutId) {
        clearTimeout(this.refreshTimeoutId);
        this.refreshTimeoutId = null;
      }

      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiresAt = payload.exp * 1000; // Convert to ms
      const now = Date.now();
      
      // Refresh 1 minute before expiration
      const refreshTime = expiresAt - now - (60 * 1000);

      if (refreshTime > 0) {
        console.log(`Scheduling token refresh in ${Math.round(refreshTime / 1000)} seconds`);
        this.refreshTimeoutId = setTimeout(() => {
          this.refreshAccessToken().catch(err => console.error('Scheduled refresh failed', err));
        }, refreshTime);
      } else {
        // If already expired or close to expiring, try to refresh immediately
        // But only if we have a refresh token
        if (this.getRefreshToken()) {
           this.refreshAccessToken().catch(err => console.error('Immediate refresh failed', err));
        }
      }
    } catch (error) {
      console.error('Failed to schedule refresh:', error);
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

  // Check if the user is authenticated and validate token
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    
    // Check token expiration
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      
      // If token is expired, DO NOT remove it immediately.
      // Let the API interceptor or scheduled refresh handle it.
      // Just return false to indicate "not currently valid access token".
      if (payload.exp <= currentTime) {
        return false;
      }
      
      return true;
    } catch (err) {
      // If there's an error parsing the token, remove it from storage
      this.removeToken();
      // Log the error for debugging purposes
      console.error('Token validation failed:', err);
      return false;
    }
  }

  // Helper to handle response errors
  private async handleResponseError(response: Response): Promise<never> {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let errorData: any;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      try {
        errorData = await response.json();
      } catch {
        errorData = await response.text();
      }
    } else {
      errorData = await response.text();
    }

    // If errorData is an object, try to extract a message
    const message = typeof errorData === 'object' && errorData?.message 
      ? errorData.message 
      : (typeof errorData === 'string' ? errorData : `HTTP error! status: ${response.status}`);

    console.error('Auth request failed:', response.status, errorData);
    throw new Error(message);
  }

  // Sign in user
  async signin(request: SigninRequest, rememberMe: boolean = false): Promise<AuthenticationResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/sign-in`, {
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
    } catch (error) {
      console.error('Signin failed:', error);
      throw error;
    }
  }

  // Sign up user
  async signup(request: SignupRequest): Promise<MessageResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/sign-up`, {
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
    } catch (error) {
      console.error('Signup failed:', error);
      throw error;
    }
  }

  // Verify email
  async verify(request: VerificationRequest): Promise<MessageResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/verify`, {
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
    } catch (error) {
      console.error('Verification failed:', error);
      throw error;
    }
  }

  // Resend verification email
  async resendVerification(email: string): Promise<MessageResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/resend-verification?email=${encodeURIComponent(email)}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        await this.handleResponseError(response);
      }

      return await response.json();
    } catch (error) {
      console.error('Resend verification failed:', error);
      throw error;
    }
  }

  // Refresh access token
  async refreshAccessToken(): Promise<AuthenticationResponse> {
    if (this.refreshPromise) {
      return this.refreshPromise;
    }

    this.refreshPromise = (async () => {
      try {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        const response = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
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
      } catch (error) {
        console.error('Token refresh failed:', error);
        throw error;
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
        await fetch(`${API_BASE_URL}/api/v1/auth/logout`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.getToken()}`,
          },
          body: JSON.stringify({ refreshToken }),
        });
      }
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      this.removeToken();
    }
  }

  // Logout from all devices
  async logoutAllDevices(): Promise<MessageResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/logout-all`, {
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
    } catch (error) {
      console.error('Logout all devices failed:', error);
      throw error;
    }
  }

  // Fetch auth-related configuration (e.g., resend cooldown)
  async getAuthConfig(): Promise<AuthConfigResponse> {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/config`, {
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
}

const authService = new AuthService();
export default authService;