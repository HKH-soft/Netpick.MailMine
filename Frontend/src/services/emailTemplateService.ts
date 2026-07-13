// emailTemplateService.ts
import api from './api';

export interface EmailTemplate {
  id: string;
  name: string;
  description?: string;
  category: 'WELCOME' | 'QUOTE' | 'SUPPORT' | 'REMINDER' | 'THANK_YOU' | 'NEWSLETTER' | 'PROMOTIONAL' | 'CUSTOM';
  subjectTemplate: string;
  bodyTemplate: string;
  isShared: boolean;
}

class EmailTemplateService {
  private basePath = '/api/v1/mailmine/email-templates';

  public async listTemplates(): Promise<EmailTemplate[]> {
    return await api.get<EmailTemplate[]>(`${this.basePath}`);
  }

  public async getTemplate(id: string): Promise<EmailTemplate> {
    return await api.get<EmailTemplate>(`${this.basePath}/${id}`);
  }

  public async createTemplate(template: Partial<EmailTemplate>): Promise<EmailTemplate> {
    return await api.post<EmailTemplate>(`${this.basePath}`, template);
  }

  public async updateTemplate(id: string, template: Partial<EmailTemplate>): Promise<EmailTemplate> {
    return await api.put<EmailTemplate>(`${this.basePath}/${id}`, template);
  }

  public async deleteTemplate(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }
}

const emailTemplateService = new EmailTemplateService();
export default emailTemplateService;


