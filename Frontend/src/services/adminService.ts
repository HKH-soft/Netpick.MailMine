// adminService.ts
import api from './api';
import { SignupRequest } from './authService';

class AdminService {
  private basePath = '/api/v1/admin';

  /**
   * Create a new user account (admin-initiated, skips normal signup)
   */
  public async createUser(request: SignupRequest): Promise<void> {
    await api.post<void>(`${this.basePath}/users`, request);
  }

  /**
   * Create a new admin account (super admin only)
   */
  public async createAdmin(request: SignupRequest): Promise<void> {
    await api.post<void>(`${this.basePath}/admins`, request);
  }
}

const adminService = new AdminService();
export default adminService;