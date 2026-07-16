// rulesService.ts
import api from './api';

export enum ConditionType {
  SENDER_CONTAINS = "SENDER_CONTAINS",
  SUBJECT_CONTAINS = "SUBJECT_CONTAINS",
  BODY_CONTAINS = "BODY_CONTAINS",
  HAS_ATTACHMENT = "HAS_ATTACHMENT",
  TAG_MATCHES = "TAG_MATCHES",
}

export enum ActionType {
  ASSIGN_TO_USER = "ASSIGN_TO_USER",
  ADD_TAG = "ADD_TAG",
  MOVE_TO_FOLDER = "MOVE_TO_FOLDER",
  MARK_AS_READ = "MARK_AS_READ",
  SEND_NOTIFICATION = "SEND_NOTIFICATION",
}

export interface EmailRule {
  id: string;
  name: string;
  description?: string;
  conditionType: ConditionType;
  conditionValue: string;
  actionType: ActionType;
  actionValue?: string;
  priority: number;
  isActive: boolean;
  createdBy?: string;
}

class RulesService {
  private basePath = '/api/v1/mailmine/email-rules';

  public async listRules(): Promise<EmailRule[]> {
    return await api.get<EmailRule[]>(this.basePath);
  }

  public async getRule(id: string): Promise<EmailRule> {
    return await api.get<EmailRule>(`${this.basePath}/${id}`);
  }

  public async createRule(rule: Partial<EmailRule>): Promise<EmailRule> {
    return await api.post<EmailRule>(this.basePath, rule);
  }

  public async updateRule(id: string, rule: Partial<EmailRule>): Promise<EmailRule> {
    return await api.put<EmailRule>(`${this.basePath}/${id}`, rule);
  }

  public async deleteRule(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  public async testRule(id: string, emailMessage: unknown): Promise<void> {
    await api.post(`${this.basePath}/${id}/test`, emailMessage);
  }
}

const rulesService = new RulesService();
export default rulesService;