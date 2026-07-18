// attachmentService.ts
import api from './api';

export interface Attachment {
  filename: string;
  originalName: string;
  mimeType: string;
  size: number;
  url?: string;
}

class AttachmentService {
  private basePath = '/api/v1/mailmine/attachments';

  public async uploadAttachment(emailId: string, file: File): Promise<Attachment> {
    const formData = new FormData();
    formData.append('file', file);
    return await api.post<Attachment>(`${this.basePath}/email/${emailId}`, formData);
  }

  public async getAttachments(emailId: string): Promise<Attachment[]> {
    return await api.get<Attachment[]>(`${this.basePath}/email/${emailId}`);
  }

  public async searchAttachments(query: string): Promise<Attachment[]> {
    return await api.get<Attachment[]>(`${this.basePath}/search?query=${encodeURIComponent(query)}`);
  }

public async deleteAttachment(filename: string): Promise<void> {
    await api.delete(`${this.basePath}/${encodeURIComponent(filename)}`);
  }
}

const attachmentService = new AttachmentService();
export default attachmentService;