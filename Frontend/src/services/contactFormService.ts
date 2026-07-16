import api from './api';

export interface ContactFormData {
  name: string;
  email: string;
  subject: string;
  message: string;
}

export interface ContactFormResponse {
  message: string;
}

class ContactFormService {
  private basePath = '/api/v1/core/contact';

  public async submitContactForm(data: ContactFormData): Promise<ContactFormResponse> {
    return await api.post<ContactFormResponse>(this.basePath, data);
  }
}

const contactFormService = new ContactFormService();
export default contactFormService;
