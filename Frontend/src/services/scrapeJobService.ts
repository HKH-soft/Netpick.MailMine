// scrapeJobService.ts
import api, { ApiResponse } from './api';

export interface ScrapeJob {
  id: string;
  link: string;
  attempt: number;
  beenScraped: boolean;
  scrapeFailed: boolean;
  description: string;
  createdAt: string;
  updatedAt: string;
}

class ScrapeJobService {
  private basePath = '/scrape_job';

  public async getAllScrapeJobs(page: number = 1): Promise<ApiResponse<ScrapeJob[]>> {
    return await api.get<ApiResponse<ScrapeJob[]>>(`${this.basePath}?page=${page}`);
  }

  public async getScrapeJobById(id: string): Promise<ScrapeJob> {
    return await api.get<ScrapeJob>(`${this.basePath}/${id}`);
  }

  public async createScrapeJob(link: string, description: string): Promise<ScrapeJob> {
    return await api.post<ScrapeJob>(this.basePath, { link, description });
  }

  public async updateScrapeJob(id: string, updates: Partial<ScrapeJob>): Promise<ScrapeJob> {
    return await api.put<ScrapeJob>(`${this.basePath}/${id}`, updates);
  }

  public async deleteScrapeJob(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }
}

const scrapeJobService = new ScrapeJobService();
export default scrapeJobService;