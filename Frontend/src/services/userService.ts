// userService.ts
import api from './api';

export interface User {
  id: string;
  email: string;
  name: string;
  role: string;
  preference: string;
  profileImageId: string;
  created_at: string;
  updatedAt: string;
  lastLoginAt: string;
}

export interface AllUsersResponse {
  users: User[];
  totalPageCount: number;
  currentPage: number;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
}

class UserService {
  private basePath = '/users';

  public async getAllUsers(page: number = 1): Promise<AllUsersResponse> {
    return await api.get<AllUsersResponse>(`${this.basePath}?page=${page}`);
  }

  public async getUserProfile(): Promise<User> {
    // This would typically be handled with an auth header
    return await api.get<User>('/profile');
  }
  // public async updateUserProfile(): Promise<User> {
  //   // This would typically be handled with an auth header
  //   return await api.put<User>('/profile');
  // }
  // public async deleteUserProfile(): Promise<User> {
  //   // This would typically be handled with an auth header
  //   return await api.delete<User>('/profile');
  // }

  public async createUser(request: CreateUserRequest): Promise<User> {
    await api.post<Record<string, never>>('/auth/sign-up', request as unknown as Record<string, unknown>);
    // Return a mock user object since the API doesn't return user data
    return {
      id: '',
      email: request.email,
      name: request.name,
      role: 'USER',
      preference: '',
      profileImageId: '',
      created_at: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      lastLoginAt: new Date().toISOString()
    };
  }

  public async createAdmin(request: CreateUserRequest): Promise<User> {
    await api.post<Record<string, never>>('/admins/create', request as unknown as Record<string, unknown>);
    // Return a mock user object since the API doesn't return user data
    return {
      id: '',
      email: request.email,
      name: request.name,
      role: 'ADMIN',
      preference: '',
      profileImageId: '',
      created_at: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      lastLoginAt: new Date().toISOString()
    };
  }
}

const userService = new UserService();
export default userService;