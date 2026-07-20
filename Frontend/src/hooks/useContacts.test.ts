import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useContacts, useContact } from './useContacts';
import ContactService from '@/services/contactService';
import { PageDTO } from '@/services/api';

vi.mock('@/services/contactService', () => ({
  default: {
    getAllContacts: vi.fn(),
    getContactById: vi.fn(),
  },
}));

describe('useContacts hook', () => {
  const mockContacts = [
    { id: '1', emails: ['contact1@example.com'], createdAt: '', updatedAt: '' },
    { id: '2', emails: ['contact2@example.com'], createdAt: '', updatedAt: '' },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches contacts on successful request', async () => {
    const mockResponse: PageDTO<typeof mockContacts[0]> = {
      content: mockContacts,
      totalPages: 1,
      totalElements: 2,
      currentPage: 1,
      pageSize: 10,
      numberOfElements: 2,
      hasNext: false,
      hasPrevious: false,
      isFirst: true,
      isLast: true,
    };

    (ContactService.getAllContacts as any).mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useContacts(1));

    expect(result.current.loading).toBe(true);

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.contacts).toEqual(mockContacts);
    expect(result.current.error).toBeNull();
  });

  it('handles fetch error gracefully', async () => {
    (ContactService.getAllContacts as any).mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => useContacts(1));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.error).toBe('Failed to fetch contacts');
    expect(result.current.contacts).toEqual([]);
  });
});

describe('useContact hook', () => {
  const mockContact = { id: '1', emails: ['test@example.com'], createdAt: '', updatedAt: '' };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches contact by ID on successful request', async () => {
    (ContactService.getContactById as any).mockResolvedValue(mockContact);

    const { result } = renderHook(() => useContact('1'));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.contact).toEqual(mockContact);
    expect(result.current.error).toBeNull();
  });

  it('does not fetch when id is null', async () => {
    const { result } = renderHook(() => useContact(null));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(ContactService.getContactById).not.toHaveBeenCalled();
  });

  it('handles fetch error gracefully', async () => {
    (ContactService.getContactById as any).mockRejectedValue(new Error('Not found'));

    const { result } = renderHook(() => useContact('999'));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.error).toBe('Failed to fetch contact');
  });
});