// api.ts
import AuthService from './authService';
import { mockDashboardStats, mockCampaigns, mockContacts, mockDeals, createMockPage } from './mockData';

// Use relative URL to go through Next.js proxy
const API_BASE_URL = '';

// Check if dev mode is enabled (mock data, no auth required)
const isDevMode = process.env.NEXT_PUBLIC_DEV_MODE === 'true';

export class ApiError<T = unknown> extends Error {
  status: number;
  data?: T;

  constructor(message: string, status: number, data?: T) {
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

// Get mock data based on endpoint
function getMockData<T>(endpoint: string): T | null {
  if (!isDevMode) return null;

  // Dashboard stats
  if (endpoint.includes('/dashboard/stats') || endpoint.includes('/dashboard')) {
    return mockDashboardStats as T;
  }

  // Campaigns
  if (endpoint.includes('/campaigns')) {
    return createMockPage(mockCampaigns) as T;
  }

  // Contacts
  if (endpoint.includes('/contacts')) {
    return createMockPage(mockContacts) as T;
  }

  // Deals
  if (endpoint.includes('/deals')) {
    return createMockPage(mockDeals) as T;
  }

  // Users
  if (endpoint.includes('/users')) {
    return createMockPage([{ id: '1', email: 'dev@example.com', name: 'Development User', role: 'SUPER_ADMIN' }]) as T;
  }

  // Products
  if (endpoint.includes('/products')) {
    return createMockPage([{ id: '1', name: 'Sample Product', price: 99.99, description: 'Mock product for dev mode' }]) as T;
  }

  // Tasks
  if (endpoint.includes('/tasks')) {
    return createMockPage([{ id: '1', title: 'Sample Task', status: 'TODO', priority: 'HIGH' }]) as T;
  }

  // Projects
  if (endpoint.includes('/projects')) {
    return createMockPage([{ id: '1', name: 'Sample Project', status: 'ACTIVE', progress: 50 }]) as T;
  }

  return null;
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
    // In dev mode, return mock data instead of making real API calls
    if (isDevMode) {
      const mockData = getMockData<T>(endpoint);
      if (mockData) {
        console.debug(`[DEV MODE] Returning mock data for ${endpoint}`);
        return mockData;
      }
      // For unhandled endpoints, return empty object
      console.debug(`[DEV MODE] No mock data for ${endpoint}, returning empty object`);
      return {} as T;
    }

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

        let errorData: unknown;
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

  public get<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET', ...options });
  }

  public post<T>(endpoint: string, data: unknown, options: RequestInit = {}): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
      ...options,
    });
  }

  public put<T>(endpoint: string, data: unknown, options: RequestInit = {}): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
      ...options,
    });
  }

  public delete<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE', ...options });
  }

  /**
   * Create a cancelable request. Returns the promise and a cancel function.
   */
  public requestCancelable<T>(endpoint: string, options: RequestInit = {}) {
    const controller = new AbortController();
    const signal = controller.signal;
    const promise = this.request<T>(endpoint, { ...options, signal });
    return {
      promise,
      cancel: () => controller.abort(),
    };
  }
}

const apiService = new ApiService();
export default apiService;


