// scrapeService.ts
import api from './api';

export type PipelineStageEnum = 
  | 'STARTED'
  | 'API_CALLER_STARTED'
  | 'API_CALLER_COMPLETE'
  | 'SCRAPER_STARTED'
  | 'SCRAPER_COMPLETE'
  | 'PARSER_STARTED'
  | 'PARSER_COMPLETE';

export interface Pipeline {
  id: string;
  stage: PipelineStageEnum;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface PipelineStatusResponse {
  active: boolean;
  message: string;
}

class ScrapeService {
  private basePath = '/api/v1/scrape';

  /**
   * Start Google search step
   */
  public async startGoogleSearch(): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/start_google`, {});
  }

  /**
   * Start scraping step
   */
  public async startScraping(): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/start_scrape`, {});
  }

  /**
   * Start extraction step
   */
  public async startExtract(): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/start_extract`, {});
  }

  /**
   * Execute specific pipeline steps
   */
  public async executeSteps(steps: PipelineStageEnum[]): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/execute_steps`, steps);
  }

  /**
   * Execute all pipeline steps
   */
  public async executeAll(): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/execute_all`, {});
  }

  // ==================== Pipeline Control Endpoints ====================

  /**
   * Pause current pipeline
   */
  public async pausePipeline(): Promise<Pipeline> {
    return await api.post<Pipeline>(`${this.basePath}/pause`, {});
  }

  /**
   * Resume current pipeline
   */
  public async resumePipeline(): Promise<Pipeline> {
    return await api.post<Pipeline>(`${this.basePath}/resume`, {});
  }

  /**
   * Skip current step
   */
  public async skipCurrentStep(): Promise<Pipeline> {
    return await api.post<Pipeline>(`${this.basePath}/skip`, {});
  }

  /**
   * Cancel current pipeline
   */
  public async cancelPipeline(): Promise<Pipeline> {
    return await api.post<Pipeline>(`${this.basePath}/cancel`, {});
  }

  /**
   * Get current pipeline status
   */
  public async getPipelineStatus(): Promise<PipelineStatusResponse> {
    return await api.get<PipelineStatusResponse>(`${this.basePath}/status`);
  }
}

const scrapeService = new ScrapeService();
export default scrapeService;