// api.ts
import AuthService from './authService';

// Use relative URL to go through Next.js proxy
const API_BASE_URL = '';

export class ApiError extends Error {
  status: number;
  data: any;

  constructor(message: string, status: number, data: any) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

export interface PageDTO<T> {
  context: T[];
  totalPageCount: number;
  currentPage: number;
}

export interface ApiResponse<T> {
  content: T;
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

class ApiService {
  private baseUrl: string;
  private isRefreshing: boolean = false;
  private refreshPromise: Promise<boolean> | null = null;

  constructor(baseUrl: string = API_BASE_URL) {
    this.baseUrl = baseUrl;
  }

  private async tryRefreshToken(): Promise<boolean> {
    // If already refreshing, wait for that to complete
    if (this.isRefreshing && this.refreshPromise) {
      return this.refreshPromise;
    }

    this.isRefreshing = true;
    this.refreshPromise = (async () => {
      try {
        await AuthService.refreshAccessToken();
        return true;
      } catch (error) {
        console.error('Token refresh failed:', error);
        return false;
      } finally {
        this.isRefreshing = false;
        this.refreshPromise = null;
      }
    })();

    return this.refreshPromise;
  }

  public async request<T>(endpoint: string, options: RequestInit = {}, isRetry: boolean = false): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;
    
    // Get the auth token
    const token = AuthService.getToken();
    
    const defaultHeaders: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    };

    const config: RequestInit = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...(options.headers as Record<string, string>),
      },
    };

    try {
      const response = await fetch(url, config);
      
      if (!response.ok) {
        // If we get a 401, try to refresh the token (but only once)
        if (response.status === 401 && !isRetry) {
          const refreshed = await this.tryRefreshToken();
          if (refreshed) {
            // Retry the request with the new token
            return this.request<T>(endpoint, options, true);
          } else {
            // Refresh failed, redirect to login
            AuthService.removeToken();
            if (typeof window !== 'undefined') {
              window.location.href = '/signin';
            }
          }
        }
        
        // If we get a 403, remove the token and redirect to login
        if (response.status === 403) {
          AuthService.removeToken();
          if (typeof window !== 'undefined') {
            window.location.href = '/signin';
          }
        }

        let errorData: any;
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
          try {
            errorData = await response.json();
          } catch (e) {
            errorData = await response.text();
          }
        } else {
          errorData = await response.text();
        }

        throw new ApiError(
          `HTTP error! status: ${response.status}`,
          response.status,
          errorData
        );
      }
      
      // Check if response has content before trying to parse JSON
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      } else {
        // Return empty object or null for responses without JSON body
        return {} as T;
      }
    } catch (error) {
      console.error(`API request failed: ${error}`);
      throw error;
    }
  }

  public get<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' });
  }

  public post<T>(endpoint: string, data: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  public put<T>(endpoint: string, data: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  public delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }
}

const apiService = new ApiService();
export default apiService;