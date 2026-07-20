import { describe, it, expect, vi, beforeEach } from 'vitest';
import contactService from './contactService';
import api, { PageDTO } from './api';

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('ContactService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls getAllContacts with correct endpoint', async () => {
    const mockResponse: PageDTO<{ id: string; emails: string[] }> = {
      content: [],
      totalPages: 1,
      totalElements: 0,
      currentPage: 1,
      pageSize: 10,
      numberOfElements: 0,
      hasNext: false,
      hasPrevious: false,
      isFirst: true,
      isLast: true,
    };

    (api.get as any).mockResolvedValue(mockResponse);

    const result = await contactService.getAllContacts();

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/contacts'));
    expect(result).toEqual(mockResponse);
  });

  it('calls getContactById with correct endpoint', async () => {
    const mockContact = { id: '123', emails: ['test@example.com'], createdAt: '', updatedAt: '' };
    (api.get as any).mockResolvedValue(mockContact);

    const result = await contactService.getContactById('123');

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/contacts/123'));
    expect(result).toEqual(mockContact);
  });

  it('calls getStats with correct endpoint', async () => {
    (api.get as any).mockResolvedValue({ total: 100 });

    const result = await contactService.getStats();

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/contacts/stats'));
    expect(result).toEqual({ total: 100 });
  });

  it('calls deleteContact correctly', async () => {
    (api.delete as any).mockResolvedValue(undefined);

    await contactService.deleteContact('123');

    expect(api.delete).toHaveBeenCalledWith(expect.stringContaining('/contacts/123'));
  });

  it('calls restoreContact correctly', async () => {
    (api.put as any).mockResolvedValue(undefined);

    await contactService.restoreContact('123');

    expect(api.put).toHaveBeenCalledWith(expect.stringContaining('/contacts/123/restore'), {});
  });
});