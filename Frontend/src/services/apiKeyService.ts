// apiKeyService.ts
import api, { ApiResponse } from './api';

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
  point: number;
  searchEngineId: string;
  apiLink: string;
  description: string;
}

class ApiKeyService {
  private basePath = '/apikey';

  public async getAllApiKeys(page: number = 1): Promise<ApiResponse<ApiKey[]>> {
    return await api.get<ApiResponse<ApiKey[]>>(`${this.basePath}?page=${page}`);
  }

  public async getApiKeyById(id: string): Promise<ApiKey> {
    return await api.get<ApiKey>(`${this.basePath}/${id}`);
  }

  public async createApiKey(data: ApiKeyRequest): Promise<ApiKey> {
    return await api.post<ApiKey>(this.basePath, data as unknown as Record<string, unknown>);
  }

  public async updateApiKey(id: string, data: ApiKeyRequest): Promise<ApiKey> {
    return await api.put<ApiKey>(`${this.basePath}/${id}`, data as unknown as Record<string, unknown>);
  }

  public async deleteApiKey(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }
}

const apiKeyService = new ApiKeyService();
export default apiKeyService;