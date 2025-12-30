// scrapeDataService.ts
import api, { PageDTO } from './api';

export interface ScrapeData {
  id: string;
  fileName: string;
  attemptNumber: number;
  parsed: boolean;
  createdAt: string;
  updatedAt: string;
}

class ScrapeDataService {
  private basePath = '/api/v1/scrape/scrape_data';

  /**
   * Get all active scrape data
   */
  public async getAllScrapeData(page: number = 1, options: RequestInit = {}): Promise<PageDTO<ScrapeData>> {
    return await api.get<PageDTO<ScrapeData>>(`${this.basePath}?page=${page}`, options);
  }

  /**
   * Get all deleted scrape data
   */
  public async getDeletedScrapeData(page: number = 1): Promise<PageDTO<ScrapeData>> {
    return await api.get<PageDTO<ScrapeData>>(`${this.basePath}/deleted?page=${page}`);
  }

  /**
   * Get all scrape data including deleted
   */
  public async getAllScrapeDataIncludingDeleted(page: number = 1): Promise<PageDTO<ScrapeData>> {
    return await api.get<PageDTO<ScrapeData>>(`${this.basePath}/all?page=${page}`);
  }

  /**
   * Get scrape data by ID
   */
  public async getScrapeDataById(id: string, options: RequestInit = {}): Promise<ScrapeData> {
    return await api.get<ScrapeData>(`${this.basePath}/${id}`, options);
  }

  /**
   * Get deleted scrape data by ID
   */
  public async getDeletedScrapeDataById(id: string): Promise<ScrapeData> {
    return await api.get<ScrapeData>(`${this.basePath}/deleted/${id}`);
  }

  /**
   * Restore a soft-deleted scrape data
   */
  public async restoreScrapeData(id: string): Promise<void> {
    await api.put<void>(`${this.basePath}/${id}/restore`, {});
  }

  /**
   * Soft delete scrape data
   */
  public async deleteScrapeData(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Permanently delete scrape data
   */
  public async fullDeleteScrapeData(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}/full_delete`);
  }
}

const scrapeDataService = new ScrapeDataService();
export default scrapeDataService;