// labelService.ts
import api, { PageDTO } from './api';
import { Label } from './taskService';

export interface LabelCreateRequest {
  name: string;
  color: string;
  projectId?: string;
  createdById?: string;
}

class LabelService {
  private basePath = '/api/v1/taskfarm/labels';

  /**
   * Get all labels
   */
  public async getAllLabels(page: number = 1): Promise<PageDTO<Label>> {
    return await api.get<PageDTO<Label>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get labels by project
   */
  public async getLabelsByProject(projectId: string): Promise<Label[]> {
    return await api.get<Label[]>(`${this.basePath}/project/${projectId}`);
  }

  /**
   * Create a new label
   */
  public async createLabel(label: LabelCreateRequest): Promise<Label> {
    return await api.post<Label>(this.basePath, label);
  }

  /**
   * Update an existing label
   */
  public async updateLabel(id: string, label: LabelCreateRequest): Promise<Label> {
    return await api.put<Label>(`${this.basePath}/${id}`, label);
  }

  /**
   * Delete a label
   */
  public async deleteLabel(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Add label to task
   */
  public async addLabelToTask(taskId: string, labelId: string): Promise<void> {
    await api.post(`${this.basePath}/tasks/${taskId}/labels/${labelId}`, {});
  }

  /**
   * Remove label from task
   */
  public async removeLabelFromTask(taskId: string, labelId: string): Promise<void> {
    await api.delete(`${this.basePath}/tasks/${taskId}/labels/${labelId}`);
  }

  /**
   * Get labels for task
   */
  public async getLabelsForTask(taskId: string): Promise<Label[]> {
    return await api.get<Label[]>(`${this.basePath}/tasks/${taskId}`);
  }
}

const labelService = new LabelService();
export default labelService;