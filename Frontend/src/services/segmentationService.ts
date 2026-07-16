// segmentationService.ts
import api from './api';

export interface CustomerSegment {
  segment: string;
  count: number;
}

export interface TopCustomer {
  id: string;
  email: string;
  contactCount: number;
  totalValue: number;
}

class SegmentationService {
  private basePath = '/api/v1/mailmine/segments';

  public async segmentByActivity(): Promise<Record<string, number>> {
    return await api.get<Record<string, number>>(`${this.basePath}/activity`);
  }

  public async getTopCustomers(limit: number = 10): Promise<TopCustomer[]> {
    return await api.get<TopCustomer[]>(`${this.basePath}/top-customers?limit=${limit}`);
  }
}

const segmentationService = new SegmentationService();
export default segmentationService;