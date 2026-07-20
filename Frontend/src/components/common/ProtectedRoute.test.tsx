import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { useAuth } from '@/context/AuthContext';
import ProtectedRoute from './ProtectedRoute';
import AuthService from '@/services/authService';

const mockPush = vi.fn();

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
    replace: vi.fn(),
    refresh: vi.fn(),
  }),
  usePathname: () => '/dashboard',
}));

vi.mock('@/context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

vi.mock('@/services/authService', () => ({
  default: {
    getToken: vi.fn(),
    isAuthenticated: vi.fn(),
    removeToken: vi.fn(),
  },
}));

function makeToken(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload));
  return `${header}.${body}.signature`;
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (useAuth as any).mockReturnValue({ isAuthenticated: true });
    process.env.NEXT_PUBLIC_DEV_MODE = undefined;
  });

  describe('dev mode bypass', () => {
    it('renders children when authenticated in dev mode', async () => {
      process.env.NEXT_PUBLIC_DEV_MODE = 'true';

      render(
        <ProtectedRoute>
          <div>Protected content</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(screen.getByText('Protected content')).toBeInTheDocument();
      });
    });

    it('renders children in dev mode without calling AuthService', async () => {
      process.env.NEXT_PUBLIC_DEV_MODE = 'true';

      render(
        <ProtectedRoute>
          <div>Secret admin panel</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(screen.getByText('Secret admin panel')).toBeInTheDocument();
      });
      expect(AuthService.isAuthenticated).not.toHaveBeenCalled();
    });
  });

  describe('unauthenticated redirect', () => {
    it('redirects to /signin when not authenticated', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(false);

      render(
        <ProtectedRoute>
          <div>Protected content</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/signin');
      });
    });

    it('does not render children when not authenticated', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(false);

      render(
        <ProtectedRoute>
          <div>Protected content</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(mockPush).toHaveBeenCalled();
      });
      expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
    });
  });

  describe('authenticated without role requirements', () => {
    it('renders children when authenticated with no allowedRoles', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);

      render(
        <ProtectedRoute>
          <div>Protected content</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(screen.getByText('Protected content')).toBeInTheDocument();
      });
    });

    it('renders children when authenticated with empty allowedRoles', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);

      render(
        <ProtectedRoute allowedRoles={[]}>
          <div>Protected content</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(screen.getByText('Protected content')).toBeInTheDocument();
      });
    });
  });

  describe('role-based access control', () => {
    it('allows access when user role matches allowedRoles via direct role property', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);
      const token = makeToken({ role: 'ADMIN', exp: Math.floor(Date.now() / 1000) + 3600 });
      (AuthService.getToken as any).mockReturnValue(token);

      render(
        <ProtectedRoute allowedRoles={['ADMIN']}>
          <div>Admin panel</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(screen.getByText('Admin panel')).toBeInTheDocument();
      });
    });

    it('allows access when user role is in allowedRoles via scopes array', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);
      const token = makeToken({ scopes: ['SUPER_ADMIN'], exp: Math.floor(Date.now() / 1000) + 3600 });
      (AuthService.getToken as any).mockReturnValue(token);

      render(
        <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
          <div>Super admin panel</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(screen.getByText('Super admin panel')).toBeInTheDocument();
      });
    });

    it('allows access when user has ADMIN role even if not in allowedRoles (ADMIN wildcard)', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);
      const token = makeToken({ role: 'SUPER_ADMIN', exp: Math.floor(Date.now() / 1000) + 3600 });
      (AuthService.getToken as any).mockReturnValue(token);

      render(
        <ProtectedRoute allowedRoles={['USER']}>
          <div>User content</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(screen.getByText('User content')).toBeInTheDocument();
      });
    });

    it('denies access and redirects when role does not match', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);
      const token = makeToken({ role: 'USER', exp: Math.floor(Date.now() / 1000) + 3600 });
      (AuthService.getToken as any).mockReturnValue(token);

      render(
        <ProtectedRoute allowedRoles={['ADMIN', 'SUPER_ADMIN']}>
          <div>Admin panel</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/');
      });
      expect(screen.queryByText('Admin panel')).not.toBeInTheDocument();
    });

    it('shows Access Denied when role does not match', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);
      const token = makeToken({ role: 'USER', exp: Math.floor(Date.now() / 1000) + 3600 });
      (AuthService.getToken as any).mockReturnValue(token);

      render(
        <ProtectedRoute allowedRoles={['ADMIN']}>
          <div>Admin panel</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(screen.getByText('Access Denied')).toBeInTheDocument();
      });
    });

    it('denies access when no role found in token', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);
      const token = makeToken({ sub: 'user123', exp: Math.floor(Date.now() / 1000) + 3600 });
      (AuthService.getToken as any).mockReturnValue(token);

      render(
        <ProtectedRoute allowedRoles={['ADMIN']}>
          <div>Admin panel</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/signin');
      });
    });

    it('denies access when token has malformed JSON', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);
      const badToken = `header.${btoa('not-valid-json')}.signature`;
      (AuthService.getToken as any).mockReturnValue(badToken);

      render(
        <ProtectedRoute allowedRoles={['ADMIN']}>
          <div>Admin panel</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/signin');
      });
    });

    it('denies access when no token is available but isAuthenticated returns true', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);
      (AuthService.getToken as any).mockReturnValue(null);

      render(
        <ProtectedRoute allowedRoles={['ADMIN']}>
          <div>Admin panel</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/signin');
      });
    });

    it('allows access with scopes as object array', async () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);
      const token = makeToken({ scopes: [{ role: 'EDITOR' }], exp: Math.floor(Date.now() / 1000) + 3600 });
      (AuthService.getToken as any).mockReturnValue(token);

      render(
        <ProtectedRoute allowedRoles={['EDITOR']}>
          <div>Editor panel</div>
        </ProtectedRoute>
      );

      await vi.waitFor(() => {
        expect(screen.getByText('Editor panel')).toBeInTheDocument();
      });
    });
  });

  describe('loading state', () => {
    it('shows loading before auth check completes', () => {
      (AuthService.isAuthenticated as any).mockReturnValue(true);

      const { container } = render(
        <ProtectedRoute>
          <div>Protected content</div>
        </ProtectedRoute>
      );

      const loadingEl = container.querySelector('.flex.items-center.justify-center.h-screen');
      expect(loadingEl).toBeTruthy();
      expect(loadingEl?.textContent).toBe('Loading...');
    });
  });
});
