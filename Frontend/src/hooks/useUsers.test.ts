import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useUsers, useUser } from './useUsers';
import UserService from '@/services/userService';
import { PageDTO } from '@/services/api';

vi.mock('@/services/userService', () => ({
  default: {
    getAllUsers: vi.fn(),
    getUserById: vi.fn(),
  },
}));

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}));

describe('useUsers hook', () => {
  const mockUsers = [
    { id: '1', email: 'user1@example.com', name: 'User 1', role: 'USER', isVerified: true, created_at: '', updatedAt: '', lastLoginAt: '' },
    { id: '2', email: 'user2@example.com', name: 'User 2', role: 'ADMIN', isVerified: true, created_at: '', updatedAt: '', lastLoginAt: '' },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches and returns users on successful request', async () => {
    const mockResponse: PageDTO<typeof mockUsers[0]> = {
      content: mockUsers,
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

    (UserService.getAllUsers as any).mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useUsers(1));

    expect(result.current.loading).toBe(true);

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.users).toEqual(mockUsers);
    expect(result.current.totalPages).toBe(1);
    expect(result.current.error).toBeNull();
  });

  it('handles fetch error gracefully', async () => {
    (UserService.getAllUsers as any).mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => useUsers(1));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.error).toBe('Failed to fetch users');
  });
});

describe('useUser hook', () => {
  const mockUser = { id: '1', email: 'user@example.com', name: 'Test User', role: 'USER' };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches user by ID on successful request', async () => {
    (UserService.getUserById as any).mockResolvedValue(mockUser);

    const { result } = renderHook(() => useUser('1'));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.user).toEqual(mockUser);
    expect(result.current.error).toBeNull();
  });

  it('does not fetch when userId is null', async () => {
    const { result } = renderHook(() => useUser(null));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(UserService.getUserById).not.toHaveBeenCalled();
  });
});