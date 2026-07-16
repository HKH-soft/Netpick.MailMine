// emailQueueService.ts
import api from './api';

export interface EmailRequest {
  recipient: string;
  subject: string;
  body: string;
  templateName?: string;
  attachments?: string[];
}

export interface QueueItemResponse {
  id: string;
  status: string;
}

class EmailQueueService {
  private basePath = '/api/v1/mailmine/email-queue';

  public async queueEmail(request: EmailRequest): Promise<QueueItemResponse> {
    return await api.post<QueueItemResponse>(this.basePath, request);
  }

  public async getQueueItemStatus(itemId: string): Promise<QueueItemResponse> {
    return await api.get<QueueItemResponse>(`${this.basePath}/status/${itemId}`);
  }
}

const emailQueueService = new EmailQueueService();
export default emailQueueService;