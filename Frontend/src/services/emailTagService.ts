// emailTagService.ts
import api from './api';
import { PageDTO } from './api';

export interface EmailTag {
  id: string;
  name: string;
  description?: string;
  category: string;
  colorHex?: string;
}

export interface TagAssignment {
  id: string;
  emailMessageId: string;
  emailTagId: string;
  confidenceScore?: number;
  isAiGenerated: boolean;
}

class EmailTagService {
  private basePath = '/api/v1/email-tags';

  async listTags(page: number = 0, size: number = 50): Promise<PageDTO<EmailTag>> {
    return await api.get(`${this.basePath}?page=${page}&size=${size}`);
  }

  async listByCategory(category: string): Promise<EmailTag[]> {
    return await api.get<EmailTag[]>(`${this.basePath}/category/${category}`);
  }

  async getTag(id: string): Promise<EmailTag> {
    return await api.get<EmailTag>(`${this.basePath}/${id}`);
  }

  async createTag(tag: Partial<EmailTag>): Promise<EmailTag> {
    return await api.post<EmailTag>(this.basePath, tag);
  }

  async updateTag(id: string, tag: Partial<EmailTag>): Promise<EmailTag> {
    return await api.put<EmailTag>(`${this.basePath}/${id}`, tag);
  }

  async deleteTag(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  async assignTag(emailId: string, tagId: string): Promise<TagAssignment> {
    return await api.post<TagAssignment>(`${this.basePath}/assign/${emailId}/${tagId}`, {});
  }

  async getEmailTags(emailId: string): Promise<TagAssignment[]> {
    return await api.get<TagAssignment[]>(`${this.basePath}/email/${emailId}`);
  }
}

const emailTagService = new EmailTagService();
export default emailTagService;
