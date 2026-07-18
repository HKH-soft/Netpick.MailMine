// emailMessageService.ts
import api from './api';

export interface EmailMessage {
  id: string;
  messageId: string;
  threadId?: string;
  senderEmail: string;
  senderName?: string;
  recipients: string[];
  subject?: string;
  bodyText?: string;
  bodyHtml?: string;
  receivedAt: string;
  isRead: boolean;
  isAnswered: boolean;
  isFlagged: boolean;
  hasAttachments: boolean;
  mailboxFolder?: string;
  status: 'INBOX' | 'ASSIGNED' | 'REPLIED' | 'CLOSED' | 'SPAM' | 'ARCHIVED';
  tags?: EmailTag[];
}

export interface EmailTag {
  id: string;
  name: string;
  category: string;
  colorHex?: string;
}

class EmailMessageService {
  private basePath = '/api/v1/mailmine/email-messages';

  public async listEmails(page: number = 0, size: number = 20): Promise<EmailMessage[]> {
    const response = await api.get<{ content: EmailMessage[] }>(`${this.basePath}?page=${page}&size=${size}`);
    // Handle both paginated (content) and direct array responses
    return response.content || [];
  }

  public async getEmail(id: string): Promise<EmailMessage> {
    return await api.get<EmailMessage>(`${this.basePath}/${id}`);
  }

  public async classifyEmail(id: string): Promise<void> {
    await api.post(`${this.basePath}/${id}/classify`, {});
  }

  public async getEmailsByTag(tagId: string, page: number = 0, size: number = 20): Promise<EmailMessage[]> {
    const response = await api.get<{ content: EmailMessage[] }>(`${this.basePath}/by-tag/${encodeURIComponent(tagId)}?page=${page}&size=${size}`);
    return response.content || [];
  }
}

const emailMessageService = new EmailMessageService();
export default emailMessageService;


