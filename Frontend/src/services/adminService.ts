// adminService.ts
import api from './api';
import { SignupRequest } from './authService';

class AdminService {
  private basePath = '/admins';

  public async createAdministrator(request: SignupRequest): Promise<void> {
    // This would typically require an auth header with super admin token
    await api.post<void>(`${this.basePath}/create`, request as unknown as Record<string, unknown>);
  }
}

const adminService = new AdminService();
export default adminService;