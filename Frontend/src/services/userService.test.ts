import { describe, it, expect, vi, beforeEach } from 'vitest';
import UserService from './userService';
import api, { PageDTO } from './api';

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock('./authService', () => ({
  default: {},
}));

describe('UserService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls getCurrentUser with correct endpoint', async () => {
    const mockUser = { id: '1', email: 'test@example.com', name: 'Test', role: 'USER', isVerified: true, created_at: '', updatedAt: '', lastLoginAt: '' };
    (api.get as any).mockResolvedValue(mockUser);

    const result = await UserService.getCurrentUser();

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/users/me'));
    expect(result).toEqual(mockUser);
  });

  it('calls getAllUsers with correct endpoint', async () => {
    const mockResponse: PageDTO<{ id: string; email: string; name: string; role: string }> = {
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

    const result = await UserService.getAllUsers(1);

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/users?'));
    expect(result).toEqual(mockResponse);
  });

  it('calls getUserById with correct endpoint', async () => {
    const mockUser = { id: '123', email: 'test@example.com', name: 'Test', role: 'USER', isVerified: true, created_at: '', updatedAt: '', lastLoginAt: '' };
    (api.get as any).mockResolvedValue(mockUser);

    const result = await UserService.getUserById('123');

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/users/123'));
    expect(result).toEqual(mockUser);
  });

  it('calls updateUser correctly', async () => {
    const mockUser = { id: '123', email: 'test@example.com', name: 'Updated', role: 'ADMIN', isVerified: true, created_at: '', updatedAt: '', lastLoginAt: '' };
    (api.put as any).mockResolvedValue(mockUser);

    const result = await UserService.updateUser('123', { name: 'Updated' });

    expect(api.put).toHaveBeenCalledWith(expect.stringContaining('/users/123'), { name: 'Updated' });
    expect(result).toEqual(mockUser);
  });

  it('calls deleteUser correctly', async () => {
    (api.delete as any).mockResolvedValue(undefined);

    await UserService.deleteUser('123');

    expect(api.delete).toHaveBeenCalledWith(expect.stringContaining('/users/123'));
  });
});