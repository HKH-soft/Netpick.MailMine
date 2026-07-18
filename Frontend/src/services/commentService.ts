// commentService.ts
import api from './api';

export interface Comment {
  id: string;
  taskId: string;
  authorId: string;
  content: string;
  parentId?: string;
  createdAt: string;
  updatedAt: string;
}

class CommentService {
  private basePath = '/api/v1/taskfarm/comments';

  public async getByTask(taskId: string): Promise<Comment[]> {
    return await api.get<Comment[]>(`${this.basePath}/task/${taskId}`);
  }

  public async getById(id: string): Promise<Comment> {
    return await api.get<Comment>(`${this.basePath}/${id}`);
  }

  public async create(comment: Partial<Comment>): Promise<Comment> {
    return await api.post<Comment>(this.basePath, comment);
  }

  public async update(id: string, comment: Partial<Comment>): Promise<Comment> {
    return await api.put<Comment>(`${this.basePath}/${id}`, comment);
  }

  public async delete(id: string): Promise<void> {
    return await api.delete(`${this.basePath}/${id}`);
  }

  public async reply(parentId: string, taskId: string, authorId: string, content: string): Promise<Comment> {
    return await this.create({ taskId, authorId, content, parentId });
  }
}

const commentService = new CommentService();
export default commentService;