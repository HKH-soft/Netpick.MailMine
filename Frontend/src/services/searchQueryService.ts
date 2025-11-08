// searchQueryService.ts
import api, { ApiResponse } from './api';

export interface SearchQuery {
  id: string;
  sentence: string;
  link_count: number;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface SearchQueryRequest {
  sentence: string;
  link_count: number;
  description: string;
}

class SearchQueryService {
  private basePath = '/search_query';

  public async getAllSearchQueries(page: number = 1): Promise<ApiResponse<SearchQuery[]>> {
    return await api.get<ApiResponse<SearchQuery[]>>(`${this.basePath}?page=${page}`);
  }

  public async getSearchQueryById(id: string): Promise<SearchQuery> {
    return await api.get<SearchQuery>(`${this.basePath}/${id}`);
  }

  public async createSearchQuery(data: SearchQueryRequest): Promise<SearchQuery> {
    return await api.post<SearchQuery>(this.basePath, data as unknown as Record<string, unknown>);
  }

  public async updateSearchQuery(id: string, data: SearchQueryRequest): Promise<SearchQuery> {
    return await api.put<SearchQuery>(`${this.basePath}/${id}`, data as unknown as Record<string, unknown>);
  }

  public async deleteSearchQuery(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }
}

const searchQueryService = new SearchQueryService();
export default searchQueryService;