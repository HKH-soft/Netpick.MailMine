// taskService.ts
import api, { PageDTO } from './api';

export interface Task {
  id: string;
  title: string;
  description?: string;
  status: string;
  priority: string;
  projectId?: string;
  assigneeId?: string;
  creatorId?: string;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  order?: number;
}

export type TaskStatus = "TODO" | "IN_PROGRESS" | "IN_REVIEW" | "DONE" | "BLOCKED";

class TaskService {
  private basePath = '/api/v1/taskfarm/tasks';

  /**
   * Get all active tasks
   */
  public async getAllTasks(page: number = 1): Promise<PageDTO<Task>> {
    return await api.get<PageDTO<Task>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get task by ID
   */
  public async getTaskById(id: string): Promise<Task> {
    return await api.get<Task>(`${this.basePath}/${id}`);
  }

  /**
   * Get tasks by status
   */
  public async getTasksByStatus(status: string, page: number = 1): Promise<PageDTO<Task>> {
    return await api.get<PageDTO<Task>>(`${this.basePath}/status/${status}?page=${page}`);
  }

  /**
   * Get tasks by project
   */
  public async getTasksByProject(projectId: string, page: number = 1): Promise<PageDTO<Task>> {
    return await api.get<PageDTO<Task>>(`${this.basePath}/project/${projectId}?page=${page}`);
  }

  /**
   * Create a new task
   */
  public async createTask(task: Partial<Task>): Promise<Task> {
    return await api.post<Task>(this.basePath, task);
  }

  /**
   * Update an existing task
   */
  public async updateTask(id: string, task: Partial<Task>): Promise<Task> {
    return await api.put<Task>(`${this.basePath}/${id}`, task);
  }

  /**
   * Soft delete a task
   */
  public async deleteTask(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Restore a soft-deleted task
   */
  public async restoreTask(id: string): Promise<void> {
    await api.put(`${this.basePath}/${id}/restore`, {});
  }
}

const taskService = new TaskService();
export default taskService;