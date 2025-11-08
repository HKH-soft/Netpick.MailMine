// scrapeDataService.ts
import api, { ApiResponse } from './api';

export interface ScrapeData {
  id: string;
  fileName: string;
  attemptNumber: number;
  parsed: boolean;
  createdAt: string;
  updatedAt: string;
}

class ScrapeDataService {
  private basePath = '/scrape_data';

  public async getAllScrapeData(page: number = 1): Promise<ApiResponse<ScrapeData[]>> {
    return await api.get<ApiResponse<ScrapeData[]>>(`${this.basePath}?page=${page}`);
  }

  public async getScrapeDataById(id: string): Promise<ScrapeData> {
    return await api.get<ScrapeData>(`${this.basePath}/${id}`);
  }

  public async deleteScrapeData(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }
}

const scrapeDataService = new ScrapeDataService();
export default scrapeDataService;