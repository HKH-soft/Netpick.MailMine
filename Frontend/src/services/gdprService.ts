// gdprService.ts
import api from './api';
import { PageDTO } from './api';

export interface RetentionConfig {
  id: string;
  entityType: string;
  retentionDays: number;
  isActive: boolean;
  lastCleanupAt: string | null;
}

export interface AuditTrailEntry {
  id: string;
  entityType: string;
  entityId: string;
  action: string;
  performedById: string;
  performedByEmail: string;
  oldValues: string | null;
  newValues: string | null;
  ipAddress: string | null;
  createdAt: string;
}

class GdprService {
  private basePath = '/api/v1/gdpr';

  async getConfigs(): Promise<RetentionConfig[]> {
    return await api.get<RetentionConfig[]>(`${this.basePath}/configs`);
  }

  async updateConfig(entityType: string, retentionDays: number): Promise<RetentionConfig> {
    return await api.put<RetentionConfig>(`${this.basePath}/configs/${entityType}`, { retentionDays });
  }

  async runCleanup(): Promise<{ deletedCount: number }> {
    return await api.post(`${this.basePath}/cleanup`, {});
  }

  async getAuditTrail(entityType: string, entityId: string): Promise<AuditTrailEntry[]> {
    return await api.get<AuditTrailEntry[]>(`${this.basePath}/audit-trail/${entityType}/${entityId}`);
  }

  async getRecentAuditTrail(): Promise<AuditTrailEntry[]> {
    return await api.get<AuditTrailEntry[]>(`${this.basePath}/audit-trail/recent`);
  }
}

const gdprService = new GdprService();
export default gdprService;
