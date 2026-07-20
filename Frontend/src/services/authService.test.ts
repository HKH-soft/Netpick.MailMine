import { describe, it, expect, vi, beforeEach } from 'vitest';
import AuthService from './authService';

// Mock localStorage and sessionStorage
const createStorageMock = () => {
  const store: Record<string, string> = {};
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => { store[key] = value; }),
    removeItem: vi.fn((key: string) => { delete store[key]; }),
    clear: vi.fn(() => { Object.keys(store).forEach(k => delete store[k]); }),
  };
};

const localStorageMock = createStorageMock();
const sessionStorageMock = createStorageMock();

Object.defineProperty(window, 'localStorage', { value: localStorageMock });
Object.defineProperty(window, 'sessionStorage', { value: sessionStorageMock });

// Mock fetch
const mockFetch = vi.fn();
global.fetch = mockFetch;

describe('AuthService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    process.env.NEXT_PUBLIC_DEV_MODE = undefined;
    localStorageMock.clear();
  });

  describe('token storage', () => {
    it('stores token in localStorage when rememberMe is true', () => {
      AuthService.setToken('access-token', 'refresh-token', true);
      
      expect(localStorageMock.setItem).toHaveBeenCalledWith('auth_token', 'access-token');
      expect(localStorageMock.setItem).toHaveBeenCalledWith('refresh_token', 'refresh-token');
      expect(localStorageMock.setItem).toHaveBeenCalledWith('auth_remember', 'true');
    });

    it('stores token in sessionStorage when rememberMe is false', () => {
      AuthService.setToken('access-token', 'refresh-token', false);
      
      expect(sessionStorageMock.setItem).toHaveBeenCalledWith('auth_token', 'access-token');
      expect(localStorageMock.setItem).toHaveBeenCalledWith('auth_remember', 'false');
    });
  });

  describe('removeToken', () => {
    it('removes tokens from both storages', () => {
      AuthService.removeToken();
      
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('auth_token');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('refresh_token');
      expect(sessionStorageMock.removeItem).toHaveBeenCalledWith('auth_token');
      expect(sessionStorageMock.removeItem).toHaveBeenCalledWith('refresh_token');
    });
  });

  describe('isAuthenticated', () => {
    it('returns false when no token exists', () => {
      localStorageMock.getItem.mockReturnValue(null);
      expect(AuthService.isAuthenticated()).toBe(false);
    });

    it('returns false for expired token', () => {
      const expiredPayload = {
        id: '123',
        role: 'USER',
        exp: Math.floor(Date.now() / 1000) - 3600,
      };
      const expiredToken = [
        btoa(JSON.stringify({ alg: 'HS256' })),
        btoa(JSON.stringify(expiredPayload)),
        'signature',
      ].join('.');
      
      localStorageMock.getItem.mockReturnValue(expiredToken);
      expect(AuthService.isAuthenticated()).toBe(false);
    });

    it('returns true for valid unexpired token', () => {
      const validPayload = {
        id: '123',
        role: 'SUPER_ADMIN',
        exp: Math.floor(Date.now() / 1000) + 3600,
      };
      const validToken = [
        btoa(JSON.stringify({ alg: 'HS256' })),
        btoa(JSON.stringify(validPayload)),
        'signature',
      ].join('.');
      
      localStorageMock.getItem.mockReturnValue(validToken);
      expect(AuthService.isAuthenticated()).toBe(true);
    });
  });

  describe('signin', () => {
    it('calls correct endpoint with credentials', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({
          access_token: 'mock-access',
          refresh_token: 'mock-refresh',
          expires_in: 3600,
          token_type: 'Bearer',
        }),
      });

      await AuthService.signin({ email: 'test@example.com', password: 'password123' });

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/gatekeeper/auth/sign-in',
        expect.objectContaining({
          method: 'POST',
        })
      );
    });
  });
});