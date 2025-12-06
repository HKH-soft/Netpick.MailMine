// searchQueryService.ts
import api, { PageDTO } from './api';

export interface SearchQuery {
  id: string;
  sentence: string;
  linkCount: number;
  description: string | null;
  created_at: string;
  updatedAt: string;
}

export interface SearchQueryRequest {
  sentence: string;
  linkCount: number;
  description?: string;
}

class SearchQueryService {
  private basePath = '/api/v1/scrape/search_queries';

  /**
   * Get all active search queries
   */
  public async getAllSearchQueries(page: number = 1): Promise<PageDTO<SearchQuery>> {
    return await api.get<PageDTO<SearchQuery>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get all deleted search queries
   */
  public async getDeletedSearchQueries(page: number = 1): Promise<PageDTO<SearchQuery>> {
    return await api.get<PageDTO<SearchQuery>>(`${this.basePath}/deleted?page=${page}`);
  }

  /**
   * Get all search queries including deleted
   */
  public async getAllSearchQueriesIncludingDeleted(page: number = 1): Promise<PageDTO<SearchQuery>> {
    return await api.get<PageDTO<SearchQuery>>(`${this.basePath}/all?page=${page}`);
  }

  /**
   * Get search query by ID
   */
  public async getSearchQueryById(id: string): Promise<SearchQuery> {
    return await api.get<SearchQuery>(`${this.basePath}/${id}`);
  }

  /**
   * Get deleted search query by ID
   */
  public async getDeletedSearchQueryById(id: string): Promise<SearchQuery> {
    return await api.get<SearchQuery>(`${this.basePath}/deleted/${id}`);
  }

  /**
   * Create a new search query
   */
  public async createSearchQuery(data: SearchQueryRequest): Promise<SearchQuery> {
    return await api.post<SearchQuery>(this.basePath, data as unknown as Record<string, unknown>);
  }

  /**
   * Update a search query
   */
  public async updateSearchQuery(id: string, data: SearchQueryRequest): Promise<SearchQuery> {
    return await api.put<SearchQuery>(`${this.basePath}/${id}`, data as unknown as Record<string, unknown>);
  }

  /**
   * Restore a soft-deleted search query
   */
  public async restoreSearchQuery(id: string): Promise<void> {
    await api.put<void>(`${this.basePath}/${id}/restore`, {});
  }

  /**
   * Soft delete a search query
   */
  public async deleteSearchQuery(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Permanently delete a search query
   */
  public async fullDeleteSearchQuery(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}/full_delete`);
  }
}

const searchQueryService = new SearchQueryService();
export default searchQueryService;