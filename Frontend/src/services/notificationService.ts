// notificationService.ts
import api, { PageDTO } from './api';

export enum NotificationType {
  SYSTEM = "SYSTEM",
  SECURITY = "SECURITY",
  ACCOUNT = "ACCOUNT",
  BILLING = "BILLING",
  MESSAGE = "MESSAGE",
  SCRAPE = "SCRAPE",
  EMAIL = "EMAIL",
}

export interface Notification {
  id: string;
  title: string;
  message: string;
  type: NotificationType;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
  updatedAt: string;
}

class NotificationService {
  private basePath = '/api/v1/core/notifications';

  public async getUserNotifications(page: number = 1): Promise<PageDTO<Notification>> {
    return await api.get<PageDTO<Notification>>(`${this.basePath}?page=${page}`);
  }

  public async getUnreadNotifications(page: number = 1): Promise<PageDTO<Notification>> {
    return await api.get<PageDTO<Notification>>(`${this.basePath}/unread?page=${page}`);
  }

  public async markAllRead(): Promise<void> {
    await api.post(`${this.basePath}/mark-read`, {});
  }

  public async markOneRead(id: string): Promise<void> {
    await api.post(`${this.basePath}/${id}/mark-read`, {});
  }
}

const notificationService = new NotificationService();
export default notificationService;