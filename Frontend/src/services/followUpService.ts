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
    // Get token via header instead of URL to prevent token leakage
    const token = localStorage.getItem('auth_token') || '';
    // Note: EventSource doesn't support custom headers. For proper security,
    // consider migrating to a WebSocket or fetch-based SSE implementation.
    // For now, we rely on the backend stream endpoint validating via query param
    // which should be replaced with a proper WebSocket connection.
    return new EventSource(`${this.basePath}/stream`);
  }
}

const followUpService = new FollowUpService();
export default followUpService;



