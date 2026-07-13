// sharedInboxService.ts
import api from './api';
import { PageDTO } from './api';

export interface SharedInbox {
  id: string;
  emailAddress: string;
  name: string;
  description?: string;
  isActive: boolean;
  members: Array<{ id: string; name: string; email: string }>;
}

class SharedInboxService {
  private basePath = '/api/v1/shared-inboxes';

  async listInboxes(page: number = 0, size: number = 20): Promise<PageDTO<SharedInbox>> {
    return await api.get(`${this.basePath}?page=${page}&size=${size}`);
  }

  async getInbox(id: string): Promise<SharedInbox> {
    return await api.get<SharedInbox>(`${this.basePath}/${id}`);
  }

  async createInbox(inbox: Partial<SharedInbox>): Promise<SharedInbox> {
    return await api.post<SharedInbox>(this.basePath, inbox);
  }

  async updateInbox(id: string, inbox: Partial<SharedInbox>): Promise<SharedInbox> {
    return await api.put<SharedInbox>(`${this.basePath}/${id}`, inbox);
  }

  async deleteInbox(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  async addMember(inboxId: string, userId: string): Promise<SharedInbox> {
    return await api.post<SharedInbox>(`${this.basePath}/${inboxId}/members/${userId}`, {});
  }

  async removeMember(inboxId: string, userId: string): Promise<SharedInbox> {
    return await api.delete<SharedInbox>(`${this.basePath}/${inboxId}/members/${userId}`);
  }

  async assignEmail(inboxId: string, emailId: string, userId: string): Promise<SharedInbox> {
    return await api.post(`${this.basePath}/${inboxId}/assign/${emailId}/${userId}`, {});
  }
}

const sharedInboxService = new SharedInboxService();
export default sharedInboxService;
