// contactService.ts
import api, { ApiResponse } from './api';

export interface Contact {
  id: string;
  emails: string[];
  phoneNumbers: string[];
  linkedInUrls: string[];
  twitterHandles: string[];
  githubProfiles: string[];
  names: string[];
  createdAt: string;
  updatedAt: string;
}

class ContactService {
  private basePath = '/contacts';

  public async getAllContacts(page: number = 1): Promise<ApiResponse<Contact[]>> {
    return await api.get<ApiResponse<Contact[]>>(`${this.basePath}?page=${page}`);
  }

  public async getContactById(id: string): Promise<Contact> {
    return await api.get<Contact>(`${this.basePath}/${id}`);
  }

  public async deleteContact(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }
}

const contactService = new ContactService();
export default contactService;