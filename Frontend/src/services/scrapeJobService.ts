// scrapeJobService.ts
import api, { PageDTO } from './api';

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

export interface ScrapeJobStats {
  total: number;
  completed: number;
  failed: number;
  pending: number;
}

class ScrapeJobService {
  private basePath = '/api/v1/scrape/scrape_jobs';

  /**
   * Get all active scrape jobs
   */
  public async getAllScrapeJobs(page: number = 1): Promise<PageDTO<ScrapeJob>> {
    return await api.get<PageDTO<ScrapeJob>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get all deleted scrape jobs
   */
  public async getDeletedScrapeJobs(page: number = 1): Promise<PageDTO<ScrapeJob>> {
    return await api.get<PageDTO<ScrapeJob>>(`${this.basePath}/deleted?page=${page}`);
  }

  /**
   * Get all scrape jobs including deleted
   */
  public async getAllScrapeJobsIncludingDeleted(page: number = 1): Promise<PageDTO<ScrapeJob>> {
    return await api.get<PageDTO<ScrapeJob>>(`${this.basePath}/all?page=${page}`);
  }

  /**
   * Get scrape job by ID
   */
  public async getScrapeJobById(id: string): Promise<ScrapeJob> {
    return await api.get<ScrapeJob>(`${this.basePath}/${id}`);
  }

  /**
   * Get deleted scrape job by ID
   */
  public async getDeletedScrapeJobById(id: string): Promise<ScrapeJob> {
    return await api.get<ScrapeJob>(`${this.basePath}/deleted/${id}`);
  }

  /**
   * Restore a soft-deleted scrape job
   */
  public async restoreScrapeJob(id: string): Promise<void> {
    await api.put<void>(`${this.basePath}/${id}/restore`, {});
  }

  /**
   * Soft delete a scrape job
   */
  public async deleteScrapeJob(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Permanently delete a scrape job (super admin only)
   */
  public async fullDeleteScrapeJob(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}/full_delete`);
  }

  /**
   * Get scrape job statistics
   */
  public async getStats(): Promise<ScrapeJobStats> {
    return await api.get<ScrapeJobStats>(`${this.basePath}/stats`);
  }
}

const scrapeJobService = new ScrapeJobService();
export default scrapeJobService;