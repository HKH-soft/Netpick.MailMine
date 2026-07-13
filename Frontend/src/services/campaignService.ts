// campaignService.ts
import api from './api';
import { PageDTO } from './api';

export interface Campaign {
  id: string;
  name: string;
  description?: string;
  subjectLine: string;
  bodyHtml: string;
  bodyText?: string;
  status: 'DRAFT' | 'SCHEDULED' | 'SENDING' | 'SENT' | 'PAUSED' | 'CANCELLED' | 'FAILED';
  scheduledAt?: string;
  sentAt?: string;
  totalRecipients: number;
  totalSent: number;
  totalOpened: number;
  totalClicked: number;
  totalBounced: number;
  totalUnsubscribed: number;
  isAbTest: boolean;
  abVariant?: string;
}

export interface CampaignRecipient {
  id: string;
  recipientEmail: string;
  recipientName?: string;
  status: 'PENDING' | 'SENT' | 'DELIVERED' | 'OPENED' | 'CLICKED' | 'BOUNCED' | 'UNSUBSCRIBED' | 'FAILED';
  sentAt?: string;
  openedAt?: string;
  clickedAt?: string;
}

class CampaignService {
  private basePath = '/api/v1/campaigns';

  async listCampaigns(page: number = 0, size: number = 20): Promise<PageDTO<Campaign>> {
    return await api.get(`${this.basePath}?page=${page}&size=${size}`);
  }
  async getCampaign(id: string): Promise<Campaign> {
    return await api.get<Campaign>(`${this.basePath}/${id}`);
  }

  async createCampaign(campaign: Partial<Campaign>): Promise<Campaign> {
    return await api.post<Campaign>(this.basePath, campaign);
  }

  async updateCampaign(id: string, campaign: Partial<Campaign>): Promise<Campaign> {
    return await api.put<Campaign>(`${this.basePath}/${id}`, campaign);
  }

  async deleteCampaign(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  async addRecipients(id: string, emails: string[]): Promise<Campaign> {
    return await api.post<Campaign>(`${this.basePath}/${id}/recipients`, emails);
  }

  async scheduleCampaign(id: string, scheduledAt: string): Promise<Campaign> {
    return await api.post<Campaign>(`${this.basePath}/${id}/schedule?scheduledAt=${scheduledAt}`, {});
  }

  async sendNow(id: string): Promise<Campaign> {
    return await api.post<Campaign>(`${this.basePath}/${id}/send`, {});
  }

  async getCampaignStats(id: string): Promise<Campaign> {
    return await api.get<Campaign>(`${this.basePath}/${id}/stats`);
  }

  async getRecipients(id: string): Promise<CampaignRecipient[]> {
    return await api.get<CampaignRecipient[]>(`${this.basePath}/${id}/recipients`);
  }
}

const campaignService = new CampaignService();
export default campaignService;
