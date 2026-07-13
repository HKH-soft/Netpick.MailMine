// templateService.ts
import api from './api';

export interface EmailTemplate {
  id: string;
  name: string;
  description?: string;
  category: string;
  subjectTemplate: string;
  bodyTemplate: string;
  isShared: boolean;
}

export interface RenderedTemplate {
  templateId: string;
  templateName: string;
  subject: string;
  bodyHtml: string;
  variables: Record<string, string>;
}

class TemplateService {
  private basePath = '/api/v1/email-templates';

  async listTemplates(): Promise<EmailTemplate[]> {
    return await api.get<EmailTemplate[]>(this.basePath);
  }

  async getTemplate(id: string): Promise<EmailTemplate> {
    return await api.get<EmailTemplate>(`${this.basePath}/${id}`);
  }

  async createTemplate(template: Partial<EmailTemplate>): Promise<EmailTemplate> {
    return await api.post<EmailTemplate>(this.basePath, template);
  }

  async updateTemplate(id: string, template: Partial<EmailTemplate>): Promise<EmailTemplate> {
    return await api.put<EmailTemplate>(`${this.basePath}/${id}`, template);
  }

  async deleteTemplate(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  async renderTemplate(id: string, variables: Record<string, string>): Promise<RenderedTemplate> {
    return await api.post<RenderedTemplate>(`${this.basePath}/${id}/render`, variables);
  }
}

const templateService = new TemplateService();
export default templateService;
