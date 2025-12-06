// contactService.ts
import api, { PageDTO } from './api';

export interface Contact {
  id: string;
  emails: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ContactStats {
  total: number;
}

class ContactService {
  private basePath = '/api/v1/scrape/contacts';

  /**
   * Get all active contacts
   */
  public async getAllContacts(page: number = 1): Promise<PageDTO<Contact>> {
    return await api.get<PageDTO<Contact>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get all deleted contacts
   */
  public async getDeletedContacts(page: number = 1): Promise<PageDTO<Contact>> {
    return await api.get<PageDTO<Contact>>(`${this.basePath}/deleted?page=${page}`);
  }

  /**
   * Get all contacts including deleted
   */
  public async getAllContactsIncludingDeleted(page: number = 1): Promise<PageDTO<Contact>> {
    return await api.get<PageDTO<Contact>>(`${this.basePath}/all?page=${page}`);
  }

  /**
   * Get contact by ID
   */
  public async getContactById(id: string): Promise<Contact> {
    return await api.get<Contact>(`${this.basePath}/${id}`);
  }

  /**
   * Get deleted contact by ID
   */
  public async getDeletedContactById(id: string): Promise<Contact> {
    return await api.get<Contact>(`${this.basePath}/deleted/${id}`);
  }

  /**
   * Restore a soft-deleted contact
   */
  public async restoreContact(id: string): Promise<void> {
    await api.put<void>(`${this.basePath}/${id}/restore`, {});
  }

  /**
   * Soft delete a contact
   */
  public async deleteContact(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Permanently delete a contact
   */
  public async fullDeleteContact(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}/full`);
  }

  /**
   * Get contact statistics
   */
  public async getStats(): Promise<ContactStats> {
    return await api.get<ContactStats>(`${this.basePath}/stats`);
  }
}

const contactService = new ContactService();
export default contactService;