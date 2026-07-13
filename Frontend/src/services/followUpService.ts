// followUpService.ts
import api from './api';

export interface FollowUpItem {
  emailId: string;
  subject: string;
  sender: string;
  hoursSinceReceived: number;
  priority: 'URGENT' | 'NORMAL';
  assignedTo: string;
}

class FollowUpService {
  private basePath = '/api/v1/mailmine/follow-ups';

  async getDashboard(): Promise<FollowUpItem[]> {
    return await api.get<FollowUpItem[]>(`${this.basePath}/dashboard`);
  }

  async triggerDetection(): Promise<void> {
    await api.post(`${this.basePath}/trigger`, {});
  }

  createEventSource(): EventSource {
    const token = localStorage.getItem('accessToken') || '';
    return new EventSource(`${this.basePath}/stream?token=${token}`);
  }
}

const followUpService = new FollowUpService();
export default followUpService;



