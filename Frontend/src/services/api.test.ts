import { describe, it, expect, vi, beforeEach } from 'vitest';
import apiService, { ApiError } from './api';

const mockFetch = vi.fn();
global.fetch = mockFetch;

vi.mock('./authService', () => ({
  default: {
    getToken: vi.fn(() => null),
    refreshAccessToken: vi.fn(),
    removeToken: vi.fn(),
  },
}));

describe('ApiService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    process.env.NEXT_PUBLIC_DEV_MODE = undefined;
  });

  describe('ApiError', () => {
    it('creates error with status and data', () => {
      const error = new ApiError('Test error', 404, { message: 'Not found' });

      expect(error.message).toBe('Test error');
      expect(error.status).toBe(404);
      expect(error.data).toEqual({ message: 'Not found' });
      expect(error.name).toBe('ApiError');
    });

    it('creates error without optional data', () => {
      const error = new ApiError('Server error', 500);

      expect(error.message).toBe('Server error');
      expect(error.status).toBe(500);
      expect(error.data).toBeUndefined();
    });
  });

  describe('get method', () => {
    it('makes GET request with correct parameters', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ data: 'test' }),
        headers: { get: () => 'application/json' },
      });

      const result = await apiService.get('/test-endpoint');

      expect(mockFetch).toHaveBeenCalledWith(
        '/test-endpoint',
        expect.objectContaining({
          method: 'GET',
          headers: expect.objectContaining({
            'Content-Type': 'application/json',
          }),
        })
      );
      expect(result).toEqual({ data: 'test' });
    });

    it('includes authorization header when token exists', async () => {
      const AuthService = await import('./authService');
      (AuthService.default.getToken as any).mockReturnValue('test-jwt-token');

      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ data: 'protected' }),
        headers: { get: () => 'application/json' },
      });

      await apiService.get('/protected-endpoint');

      expect(mockFetch).toHaveBeenCalledWith(
        '/protected-endpoint',
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer test-jwt-token',
          }),
        })
      );
    });
  });

  describe('post method', () => {
    it('makes POST request with JSON body', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ created: true }),
        headers: { get: () => 'application/json' },
      });

      const data = { name: 'test' };
      const result = await apiService.post('/create', data);

      expect(mockFetch).toHaveBeenCalledWith(
        '/create',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify(data),
        })
      );
      expect(result).toEqual({ created: true });
    });

    it('handles FormData bodies without Content-Type header', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ uploaded: true }),
        headers: { get: () => 'application/json' },
      });

      const formData = new FormData();
      formData.append('file', new Blob(['test']), 'test.txt');

      await apiService.post('/upload', formData);

      expect(mockFetch).toHaveBeenCalledWith(
        '/upload',
        expect.objectContaining({
          method: 'POST',
          body: formData,
        })
      );
      const callHeaders = mockFetch.mock.calls[0][1].headers;
      expect(callHeaders['Content-Type']).toBeUndefined();
    });
  });

  describe('put method', () => {
    it('makes PUT request with JSON body', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ updated: true }),
        headers: { get: () => 'application/json' },
      });

      const result = await apiService.put('/update/1', { name: 'updated' });
      expect(mockFetch).toHaveBeenCalledWith(
        '/update/1',
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ name: 'updated' }),
        })
      );
      expect(result).toEqual({ updated: true });
    });
  });

  describe('patch method', () => {
    it('makes PATCH request with JSON body', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ patched: true }),
        headers: { get: () => 'application/json' },
      });

      const result = await apiService.patch('/patch/1', { name: 'patched' });
      expect(mockFetch).toHaveBeenCalledWith(
        '/patch/1',
        expect.objectContaining({
          method: 'PATCH',
          body: JSON.stringify({ name: 'patched' }),
        })
      );
      expect(result).toEqual({ patched: true });
    });

    it('makes PATCH request with empty body', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ toggled: true }),
        headers: { get: () => 'application/json' },
      });

      const result = await apiService.patch('/toggle/1', {});
      expect(mockFetch).toHaveBeenCalledWith(
        '/toggle/1',
        expect.objectContaining({
          method: 'PATCH',
          body: JSON.stringify({}),
        })
      );
      expect(result).toEqual({ toggled: true });
    });
  });

  describe('delete method', () => {
    it('makes DELETE request', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ deleted: true }),
        headers: { get: () => 'application/json' },
      });

      const result = await apiService.delete('/delete/1');

      expect(mockFetch).toHaveBeenCalledWith(
        '/delete/1',
        expect.objectContaining({ method: 'DELETE' })
      );
      expect(result).toEqual({ deleted: true });
    });
  });

  describe('requestCancelable', () => {
    it('returns a promise and cancel function', () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ data: 'cancelable' }),
        headers: { get: () => 'application/json' },
      });

      const result = apiService.requestCancelable('/cancelable');
      expect(result).toHaveProperty('promise');
      expect(result).toHaveProperty('cancel');
      expect(typeof result.cancel).toBe('function');
    });

    it('resolves when request succeeds', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ data: 'success' }),
        headers: { get: () => 'application/json' },
      });

      const { promise } = apiService.requestCancelable('/success');
      const result = await promise;
      expect(result).toEqual({ data: 'success' });
    });

    it('rejects when request fails', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        status: 500,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve({ message: 'Server error' }),
      });

      const { promise } = apiService.requestCancelable('/fail');
      await expect(promise).rejects.toThrow('HTTP error! status: 500');
    });
  });

  describe('error handling', () => {
    it('throws ApiError on 404 response', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        status: 404,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve({ message: 'Not found' }),
      });

      await expect(apiService.get('/not-found')).rejects.toThrow('HTTP error! status: 404');
    });

    it('throws ApiError on 500 response', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        status: 500,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve({ message: 'Server error' }),
      });

      await expect(apiService.get('/error')).rejects.toThrow('HTTP error! status: 500');
    });

    it('throws ApiError with parsed JSON error data', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        status: 422,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve({ message: 'Validation failed', field: 'email' }),
      });

      try {
        await apiService.get('/validation-error');
        expect.fail('Should have thrown');
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        expect((error as ApiError).status).toBe(422);
        expect((error as ApiError).data).toEqual({ message: 'Validation failed', field: 'email' });
      }
    });

    it('throws ApiError with text error data for non-JSON responses', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        status: 502,
        headers: { get: () => 'text/plain' },
        text: () => Promise.resolve('Bad Gateway'),
      });

      try {
        await apiService.get('/bad-gateway');
        expect.fail('Should have thrown');
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        expect((error as ApiError).status).toBe(502);
        expect((error as ApiError).data).toBe('Bad Gateway');
      }
    });

    it('returns empty object for non-JSON success responses', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        headers: { get: () => 'text/plain' },
        text: () => Promise.resolve('ok'),
      });

      const result = await apiService.get('/health');
      expect(result).toEqual({});
    });

    it('retries on 401 and succeeds with refreshed token', async () => {
      const AuthService = await import('./authService');
      (AuthService.default.refreshAccessToken as any).mockResolvedValue(true);

      mockFetch
        .mockResolvedValueOnce({
          ok: false,
          status: 401,
          headers: { get: () => 'application/json' },
          json: () => Promise.resolve({ message: 'Unauthorized' }),
        })
        .mockResolvedValueOnce({
          ok: true,
          json: () => Promise.resolve({ data: 'after-refresh' }),
          headers: { get: () => 'application/json' },
        });

      const result = await apiService.get('/retry-endpoint');
      expect(result).toEqual({ data: 'after-refresh' });
      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('redirects to /signin on 401 when refresh fails', async () => {
      const AuthService = await import('./authService');
      (AuthService.default.refreshAccessToken as any).mockResolvedValue(false);

      mockFetch.mockResolvedValue({
        ok: false,
        status: 401,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve({ message: 'Unauthorized' }),
      });

      await expect(apiService.get('/auth-required')).rejects.toThrow();
      expect(AuthService.default.removeToken).toHaveBeenCalled();
    });

    it('redirects to /signin on 403', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        status: 403,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve({ message: 'Forbidden' }),
      });

      await expect(apiService.get('/forbidden')).rejects.toThrow();
    });
  });

  describe('response handling', () => {
    it('returns JSON for content-type application/json', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ key: 'value' }),
        headers: { get: (name: string) => name === 'content-type' ? 'application/json' : null },
      });

      const result = await apiService.get('/json');
      expect(result).toEqual({ key: 'value' });
    });

    it('returns empty object when no content-type header', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({}),
        headers: { get: () => null },
      });

      const result = await apiService.get('/no-content-type');
      expect(result).toEqual({});
    });
  });
});
