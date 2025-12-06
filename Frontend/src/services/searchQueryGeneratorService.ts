// searchQueryGeneratorService.ts
import api from './api';
import { SearchQuery } from './searchQueryService';

export interface GenerateRequest {
  topic: string;
  target: string;
  count?: number;
}

export interface VariationRequest {
  originalQuery: string;
  count?: number;
}

export interface SiteQueryRequest {
  topic: string;
  site: string;
  count?: number;
}

export interface EmailQueryRequest {
  industry: string;
  region?: string;
  count?: number;
}

export interface GenerateResponse {
  queries: string[];
  count: number;
}

export interface GenerateAndSaveResponse {
  queries: SearchQuery[];
  saved: number;
}

export interface VariationResponse {
  original: string;
  variations: string[];
  count: number;
}

export interface SiteQueryResponse {
  site: string;
  queries: string[];
  count: number;
}

export interface EmailQueryResponse {
  industry: string;
  region: string;
  queries: string[];
  count: number;
}

class SearchQueryGeneratorService {
  private basePath = '/api/v1/search-queries/generate';

  /**
   * Generate search queries based on topic and target
   */
  public async generateQueries(request: GenerateRequest): Promise<GenerateResponse> {
    return await api.post<GenerateResponse>(this.basePath, request);
  }

  /**
   * Generate and save search queries directly to database
   */
  public async generateAndSaveQueries(request: GenerateRequest): Promise<GenerateAndSaveResponse> {
    return await api.post<GenerateAndSaveResponse>(`${this.basePath}/save`, request);
  }

  /**
   * Generate variations of an existing query
   */
  public async generateVariations(request: VariationRequest): Promise<VariationResponse> {
    return await api.post<VariationResponse>(`${this.basePath}/variations`, request);
  }

  /**
   * Generate site-restricted queries
   */
  public async generateSiteQueries(request: SiteQueryRequest): Promise<SiteQueryResponse> {
    return await api.post<SiteQueryResponse>(`${this.basePath}/site`, request);
  }

  /**
   * Generate queries for finding email addresses
   */
  public async generateEmailQueries(request: EmailQueryRequest): Promise<EmailQueryResponse> {
    return await api.post<EmailQueryResponse>(`${this.basePath}/emails`, request);
  }
}

const searchQueryGeneratorService = new SearchQueryGeneratorService();
export default searchQueryGeneratorService;
