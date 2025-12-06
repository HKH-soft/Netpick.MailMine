// pipelineService.ts
import api, { PageDTO } from './api';

export interface Pipeline {
  id: string;
  stage: string;
  state: string;
  startTime: string | null;
  endTime: string | null;
  itemsProcessed: number;
  itemsTotal: number;
  currentStepName: string | null;
  linksCreated: number;
  pagesScraped: number;
  contactsFound: number;
  errorsCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface PipelineStats {
  total: number;
  active: number;
  completed: number;
  failed: number;
  totalContactsFound: number;
}

class PipelineService {
  private basePath = '/api/v1/scrape/pipelines';

  /**
   * Get all active pipelines
   */
  public async getAllPipelines(page: number = 1): Promise<PageDTO<Pipeline>> {
    return await api.get<PageDTO<Pipeline>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get all deleted pipelines
   */
  public async getDeletedPipelines(page: number = 1): Promise<PageDTO<Pipeline>> {
    return await api.get<PageDTO<Pipeline>>(`${this.basePath}/deleted?page=${page}`);
  }

  /**
   * Get all pipelines including deleted
   */
  public async getAllPipelinesIncludingDeleted(page: number = 1): Promise<PageDTO<Pipeline>> {
    return await api.get<PageDTO<Pipeline>>(`${this.basePath}/all?page=${page}`);
  }

  /**
   * Get pipeline by ID
   */
  public async getPipelineById(id: string): Promise<Pipeline> {
    return await api.get<Pipeline>(`${this.basePath}/${id}`);
  }

  /**
   * Get deleted pipeline by ID
   */
  public async getDeletedPipelineById(id: string): Promise<Pipeline> {
    return await api.get<Pipeline>(`${this.basePath}/deleted/${id}`);
  }

  /**
   * Restore a soft-deleted pipeline
   */
  public async restorePipeline(id: string): Promise<void> {
    await api.put<void>(`${this.basePath}/${id}/restore`, {});
  }

  /**
   * Soft delete a pipeline
   */
  public async deletePipeline(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Permanently delete a pipeline
   */
  public async fullDeletePipeline(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}/full_delete`);
  }

  /**
   * Get pipeline statistics
   */
  public async getStats(): Promise<PipelineStats> {
    return await api.get<PipelineStats>(`${this.basePath}/stats`);
  }
}

const pipelineService = new PipelineService();
export default pipelineService;
