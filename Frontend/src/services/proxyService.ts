// proxyService.ts
import api, { PageDTO } from './api';

export type ProxyProtocol = 'HTTP' | 'HTTPS' | 'SOCKS4' | 'SOCKS5';
export type ProxyStatus = 'ACTIVE' | 'INACTIVE' | 'UNTESTED' | 'FAILED';

export interface Proxy {
  id: string;
  protocol: ProxyProtocol;
  host: string;
  port: number;
  username: string | null;
  status: ProxyStatus;
  lastTestedAt: string | null;
  lastUsedAt: string | null;
  successCount: number;
  failureCount: number;
  avgResponseTimeMs: number | null;
  description: string | null;
  createdAt: string;
  // V2Ray specific fields
  uuid: string | null;
  encryption: string | null;
  transport: string | null;
  security: string | null;
  sni: string | null;
  localPort: number | null;
  isV2Ray: boolean;
}

export interface ProxyRequest {
  protocol?: ProxyProtocol;
  host: string;
  port: number;
  username?: string;
  password?: string;
  description?: string;
}

export interface ProxyStats {
  total: number;
  active: number;
  inactive: number;
  untested: number;
  failed: number;
}

class ProxyService {
  private basePath = '/api/v1/proxies';

  // ==================== CRUD ====================

  /**
   * Get all active proxies
   */
  public async getAllProxies(page: number = 1): Promise<PageDTO<Proxy>> {
    return await api.get<PageDTO<Proxy>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get all deleted proxies
   */
  public async getDeletedProxies(page: number = 1): Promise<PageDTO<Proxy>> {
    return await api.get<PageDTO<Proxy>>(`${this.basePath}/deleted?page=${page}`);
  }

  /**
   * Get proxy by ID
   */
  public async getProxyById(id: string): Promise<Proxy> {
    return await api.get<Proxy>(`${this.basePath}/${id}`);
  }

  /**
   * Create a new proxy
   */
  public async createProxy(data: ProxyRequest): Promise<Proxy> {
    return await api.post<Proxy>(this.basePath, data);
  }

  /**
   * Update a proxy
   */
  public async updateProxy(id: string, data: ProxyRequest): Promise<Proxy> {
    return await api.put<Proxy>(`${this.basePath}/${id}`, data);
  }

  /**
   * Soft delete a proxy
   */
  public async deleteProxy(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Restore a soft-deleted proxy
   */
  public async restoreProxy(id: string): Promise<void> {
    await api.post<void>(`${this.basePath}/${id}/restore`, {});
  }

  /**
   * Permanently delete a proxy
   */
  public async permanentlyDeleteProxy(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}/permanent`);
  }

  // ==================== Import ====================

  /**
   * Import proxies from file
   */
  public async importProxiesFromFile(file: File): Promise<{ message: string; imported: number }> {
    const formData = new FormData();
    formData.append('file', file);

    // We need to let the browser set the Content-Type for FormData (multipart/form-data with boundary)
    // So we pass undefined for Content-Type to override the default application/json
    return await api.request<{ message: string; imported: number }>(`${this.basePath}/import`, {
      method: 'POST',
      body: formData,
      headers: {
        'Content-Type': undefined as unknown as string, 
      },
    });
  }

  /**
   * Import proxies from text
   */
  public async importProxiesFromText(proxyList: string): Promise<{ message: string; imported: number }> {
    return await api.request<{ message: string; imported: number }>(`${this.basePath}/import/text`, {
      method: 'POST',
      body: proxyList,
      headers: {
        'Content-Type': 'text/plain',
      },
    });
  }

  // ==================== Testing ====================

  /**
   * Test a specific proxy
   */
  public async testProxy(id: string): Promise<Proxy> {
    return await api.post<Proxy>(`${this.basePath}/${id}/test`, {});
  }

  /**
   * Test all untested proxies
   */
  public async testUntestedProxies(): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/test/untested`, {});
  }

  /**
   * Re-test all active proxies
   */
  public async testActiveProxies(): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/test/active`, {});
  }

  // ==================== Stats ====================

  /**
   * Get proxy statistics
   */
  public async getStats(): Promise<ProxyStats> {
    return await api.get<ProxyStats>(`${this.basePath}/stats`);
  }
}

const proxyService = new ProxyService();
export default proxyService;
