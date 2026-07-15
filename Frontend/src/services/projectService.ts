// projectService.ts
import api, { PageDTO } from './api';

export interface Project {
  id: string;
  name: string;
  description?: string;
  ownerId?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

class ProjectService {
  private basePath = '/api/v1/taskfarm/projects';

  /**
   * Get all active projects
   */
  public async getAllProjects(page: number = 1): Promise<PageDTO<Project>> {
    return await api.get<PageDTO<Project>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get project by ID
   */
  public async getProjectById(id: string): Promise<Project> {
    return await api.get<Project>(`${this.basePath}/${id}`);
  }

  /**
   * Create a new project
   */
  public async createProject(project: Partial<Project>): Promise<Project> {
    return await api.post<Project>(this.basePath, project);
  }

  /**
   * Update an existing project
   */
  public async updateProject(id: string, project: Partial<Project>): Promise<Project> {
    return await api.put<Project>(`${this.basePath}/${id}`, project);
  }

  /**
   * Soft delete a project
   */
  public async deleteProject(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Restore a soft-deleted project
   */
  public async restoreProject(id: string): Promise<void> {
    await api.put(`${this.basePath}/${id}/restore`, {});
  }

  /**
   * Get project statistics
   */
  public async getStats(id: string): Promise<Record<string, unknown>> {
    return await api.get<Record<string, unknown>>(`${this.basePath}/${id}/stats`);
  }
}

const projectService = new ProjectService();
export default projectService;