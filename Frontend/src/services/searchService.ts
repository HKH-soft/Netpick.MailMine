// searchService.ts
import api from './api';

export interface SearchResult {
  id: string;
  type: 'email' | 'tag' | 'sharedInbox' | 'campaign';
  title: string;
  subtitle: string;
  date?: string;
}

export interface GlobalSearchResponse {
  emails: SearchResult[];
  tags: SearchResult[];
  sharedInboxes: SearchResult[];
  campaigns: SearchResult[];
  totalResults: number;
}

class SearchService {
  private basePath = '/api/v1/core/search';

  async search(query: string, page: number = 0, size: number = 20): Promise<GlobalSearchResponse> {
    return await api.get<GlobalSearchResponse>(`${this.basePath}?q=${encodeURIComponent(query)}&page=${page}&size=${size}`);
  }
}

const searchService = new SearchService();
export default searchService;



