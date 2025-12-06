// userService.ts
import api, { PageDTO } from './api';

export interface User {
  id: string;
  email: string;
  name: string;
  role: string;
  isVerified: boolean;
  created_at: string;
  updatedAt: string;
  lastLoginAt: string;
}

export interface UserUpdateRequest {
  name?: string;
  preference?: string;
  description?: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}

class UserService {
  private basePath = '/api/v1/users';

  // ==================== Current User Operations ====================

  /**
   * Get current authenticated user's profile
   */
  public async getCurrentUser(): Promise<User> {
    return await api.get<User>(`${this.basePath}/me`);
  }

  /**
   * Update current authenticated user's profile
   */
  public async updateCurrentUser(request: UserUpdateRequest): Promise<User> {
    return await api.put<User>(`${this.basePath}/me`, request);
  }

  /**
   * Change current user's password
   */
  public async changePassword(request: PasswordChangeRequest): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/me/change-password`, request);
  }

  /**
   * Delete current user's account (soft delete)
   */
  public async deleteCurrentUser(): Promise<void> {
    await api.delete(`${this.basePath}/me`);
  }

  // ==================== Admin Operations ====================

  /**
   * Get all users (admin only)
   */
  public async getAllUsers(page: number = 1): Promise<PageDTO<User>> {
    return await api.get<PageDTO<User>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get a specific user by ID (admin only)
   */
  public async getUserById(userId: string): Promise<User> {
    return await api.get<User>(`${this.basePath}/${userId}`);
  }

  /**
   * Update a user by ID (admin only)
   */
  public async updateUser(userId: string, request: UserUpdateRequest): Promise<User> {
    return await api.put<User>(`${this.basePath}/${userId}`, request);
  }

  /**
   * Soft delete a user (admin only)
   */
  public async deleteUser(userId: string): Promise<void> {
    await api.delete(`${this.basePath}/${userId}`);
  }

  /**
   * Restore a soft-deleted user (admin only)
   */
  public async restoreUser(userId: string): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/${userId}/restore`, {});
  }

  /**
   * Permanently delete a user (super admin only)
   */
  public async permanentlyDeleteUser(userId: string): Promise<void> {
    await api.delete(`${this.basePath}/${userId}/permanent`);
  }

  /**
   * Send verification email to a user (admin only)
   */
  public async sendVerificationEmail(userEmail: string): Promise<{ message: string }> {
    return await api.post<{ message: string }>(`${this.basePath}/${userEmail}/send-verification`, {});
  }
}

const userService = new UserService();
export default userService;