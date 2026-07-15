// folderService.ts
import api, { PageDTO } from './api';

export interface Folder {
  id: string;
  name: string;
  parentId?: string;
  ownerId?: string;
  path?: string;
  createdAt: string;
  updatedAt: string;
}

class FolderService {
  private basePath = '/api/v1/filefarm/folders';

  /**
   * Get all active folders
   */
  public async getAllFolders(page: number = 1): Promise<PageDTO<Folder>> {
    return await api.get<PageDTO<Folder>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get folders by owner
   */
  public async getFoldersByOwner(ownerId: string): Promise<Folder[]> {
    return await api.get<Folder[]>(`${this.basePath}/owner/${ownerId}`);
  }

  /**
   * Get folder by ID
   */
  public async getFolderById(id: string): Promise<Folder> {
    return await api.get<Folder>(`${this.basePath}/${id}`);
  }

  /**
   * Create a new folder
   */
  public async createFolder(folder: Partial<Folder>): Promise<Folder> {
    return await api.post<Folder>(this.basePath, folder);
  }

  /**
   * Update an existing folder
   */
  public async updateFolder(id: string, folder: Partial<Folder>): Promise<Folder> {
    return await api.put<Folder>(`${this.basePath}/${id}`, folder);
  }

  /**
   * Soft delete a folder
   */
  public async deleteFolder(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Restore a soft-deleted folder
   */
  public async restoreFolder(id: string): Promise<void> {
    await api.put(`${this.basePath}/${id}/restore`, {});
  }
}

const folderService = new FolderService();
export default folderService;