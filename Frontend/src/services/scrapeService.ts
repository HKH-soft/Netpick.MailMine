// scrapeService.ts
import api from './api';

class ScrapeService {
  private basePath = '/scrape';

  public async startGoogleSearch(): Promise<void> {
    await api.post<void>(`${this.basePath}/start-google`, {});
  }

  public async startScraping(): Promise<void> {
    await api.post<void>(`${this.basePath}/start-scrape`, {});
  }

  public async startExtract(): Promise<void> {
    await api.post<void>(`${this.basePath}/start-extract`, {});
  }
}

const scrapeService = new ScrapeService();
export default scrapeService;