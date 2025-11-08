// authService.ts

export interface SigninRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
}

class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REMEMBER_KEY = 'auth_remember';

  // Store the JWT token based on user's "keep me logged in" choice
  setToken(token: string, rememberMe: boolean = false): void {
    if (typeof window !== 'undefined') {
      if (rememberMe) {
        // Store in localStorage for persistent login
        localStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.REMEMBER_KEY, 'true');
        // Remove from sessionStorage if it exists there
        sessionStorage.removeItem(this.TOKEN_KEY);
      } else {
        // Store in sessionStorage for session-only login
        sessionStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.REMEMBER_KEY, 'false');
        // Remove from localStorage if it exists there
        localStorage.removeItem(this.TOKEN_KEY);
      }
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

  // Remove the JWT token from storage
  removeToken(): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem(this.TOKEN_KEY);
      sessionStorage.removeItem(this.TOKEN_KEY);
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
      
      // If token is expired, remove it from storage
      if (payload.exp <= currentTime) {
        this.removeToken();
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

  // Sign in user
  async signin(request: SigninRequest, rememberMe: boolean = false): Promise<string> {
    try {
      const response = await fetch('http://localhost:8080/auth/sign-in', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const token = response.headers.get('Authorization');
      if (token) {
        // Remove Bearer prefix if present
        const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;
        this.setToken(cleanToken, rememberMe);
        return cleanToken;
      } else {
        throw new Error('No token received from server');
      }
    } catch (error) {
      console.error('Signin failed:', error);
      throw error;
    }
  }

  // Sign up user
  async signup(request: SignupRequest): Promise<string> {
    try {
      const response = await fetch('http://localhost:8080/auth/sign-up', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const token = response.headers.get('Authorization');
      if (token) {
        // Remove Bearer prefix if present
        const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;
        // By default, remember the user after signup
        this.setToken(cleanToken, true);
        return cleanToken;
      } else {
        throw new Error('No token received from server');
      }
    } catch (error) {
      console.error('Signup failed:', error);
      throw error;
    }
  }
}

const authService = new AuthService();
export default authService;