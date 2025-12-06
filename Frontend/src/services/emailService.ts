// emailService.ts
import api from './api';

export interface EmailRequest {
  recipient?: string;
  subject: string;
  body: string;
  attachment?: string;
  recipients?: string[];
  templateName?: string;
}

class EmailService {
  private basePath = '/api/v1/email';

  /**
   * Send a simple email
   */
  public async sendEmail(request: EmailRequest): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/send`, request);
  }

  /**
   * Send an email with attachment
   */
  public async sendEmailWithAttachment(request: EmailRequest): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/send-with-attachment`, request);
  }

  /**
   * Send mass emails to multiple recipients
   */
  public async sendMassEmail(request: EmailRequest): Promise<{ message: string; recipientCount: string }> {
    return await api.post<{ message: string; recipientCount: string }>(`${this.basePath}/send-mass`, request);
  }
}

const emailService = new EmailService();
export default emailService;
