// dealService.ts
import api, { PageDTO } from './api';

export interface Deal {
  id: string;
  title: string;
  description?: string;
  stage: string;
  value?: number;
  currency: string;
  contactId?: string;
  ownerId?: string;
  createdAt: string;
  updatedAt: string;
  closedAt?: string;
  probability?: number;
  expectedCloseDate?: string;
}

export interface DealStats {
  totalDeals: number;
  totalValue: number;
  winRate: number;
  dealsByStage: Record<string, number>;
}

export type DealStage = "PROSPECTING" | "QUALIFICATION" | "PROPOSAL" | "NEGOTIATION" | "CLOSED_WON" | "CLOSED_LOST";

class DealService {
  private basePath = '/api/v1/dealfarm/deals';

  /**
   * Get all active deals
   */
  public async getAllDeals(page: number = 1): Promise<PageDTO<Deal>> {
    return await api.get<PageDTO<Deal>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get deal by ID
   */
  public async getDealById(id: string): Promise<Deal> {
    return await api.get<Deal>(`${this.basePath}/${id}`);
  }

/**
    * Get deals by stage
    */
  public async getDealsByStage(stage: string, page: number = 1): Promise<PageDTO<Deal>> {
    return await api.get<PageDTO<Deal>>(`${this.basePath}/stage/${encodeURIComponent(stage)}?page=${page}`);
  }

  /**
   * Create a new deal
   */
  public async createDeal(deal: Partial<Deal>): Promise<Deal> {
    return await api.post<Deal>(this.basePath, deal);
  }

  /**
   * Update an existing deal
   */
  public async updateDeal(id: string, deal: Partial<Deal>): Promise<Deal> {
    return await api.put<Deal>(`${this.basePath}/${id}`, deal);
  }

  /**
   * Soft delete a deal
   */
  public async deleteDeal(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Restore a soft-deleted deal
   */
  public async restoreDeal(id: string): Promise<void> {
    await api.put(`${this.basePath}/${id}/restore`, {});
  }

  /**
   * Get deal statistics
   */
  public async getStats(): Promise<DealStats> {
    return await api.get<DealStats>(`${this.basePath}/stats`);
  }
}

const dealService = new DealService();
export default dealService;