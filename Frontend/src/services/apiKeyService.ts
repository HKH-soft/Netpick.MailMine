// apiKeyService.ts
import api, { PageDTO } from './api';

export interface ApiKey {
  id: string;
  key: string;
  point: number;
  apiLink: string;
  searchEngineId: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApiKeyRequest {
  key: string;
  point?: number;
  searchEngineId?: string;
  apiLink?: string;
  description?: string;
}

class ApiKeyService {
  private basePath = '/api/v1/scrape/api_keys';

  /**
   * Get all active API keys
   */
  public async getAllApiKeys(page: number = 1): Promise<PageDTO<ApiKey>> {
    return await api.get<PageDTO<ApiKey>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get all deleted API keys
   */
  public async getDeletedApiKeys(page: number = 1): Promise<PageDTO<ApiKey>> {
    return await api.get<PageDTO<ApiKey>>(`${this.basePath}/deleted?page=${page}`);
  }

  /**
   * Get all API keys including deleted
   */
  public async getAllApiKeysIncludingDeleted(page: number = 1): Promise<PageDTO<ApiKey>> {
    return await api.get<PageDTO<ApiKey>>(`${this.basePath}/all?page=${page}`);
  }

  /**
   * Get API key by ID
   */
  public async getApiKeyById(id: string): Promise<ApiKey> {
    return await api.get<ApiKey>(`${this.basePath}/${id}`);
  }

  /**
   * Get deleted API key by ID
   */
  public async getDeletedApiKeyById(id: string): Promise<ApiKey> {
    return await api.get<ApiKey>(`${this.basePath}/deleted/${id}`);
  }

  /**
   * Create a new API key
   */
  public async createApiKey(data: ApiKeyRequest): Promise<ApiKey> {
    return await api.post<ApiKey>(this.basePath, data);
  }

  /**
   * Update an API key
   */
  public async updateApiKey(id: string, data: ApiKeyRequest): Promise<ApiKey> {
    return await api.put<ApiKey>(`${this.basePath}/${id}`, data);
  }

  /**
   * Restore a soft-deleted API key
   */
  public async restoreApiKey(id: string): Promise<void> {
    await api.put<void>(`${this.basePath}/${id}/restore`, {});
  }

  /**
   * Soft delete an API key
   */
  public async deleteApiKey(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Permanently delete an API key
   */
  public async fullDeleteApiKey(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}/full_delete`);
  }
}

const apiKeyService = new ApiKeyService();
export default apiKeyService;