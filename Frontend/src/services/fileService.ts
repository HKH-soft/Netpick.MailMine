// fileService.ts
import api, { PageDTO } from './api';

export interface FileEntity {
  id: string;
  fileName: string;
  originalFileName: string;
  mimeType: string;
  fileSize?: number;
  filePath: string;
  folderId?: string;
  ownerId?: string;
  createdAt: string;
  updatedAt: string;
}

class FileService {
  private basePath = '/api/v1/filefarm/files';

  /**
   * Get all active files
   */
  public async getAllFiles(page: number = 1): Promise<PageDTO<FileEntity>> {
    return await api.get<PageDTO<FileEntity>>(`${this.basePath}?page=${page}`);
  }

  /**
   * Get file by ID
   */
  public async getFileById(id: string): Promise<FileEntity> {
    return await api.get<FileEntity>(`${this.basePath}/${id}`);
  }

  /**
   * Upload a file
   */
  public async uploadFile(file: File, folderId?: string, ownerId?: string): Promise<FileEntity> {
    const formData = new FormData();
    formData.append('file', file);
    if (folderId) formData.append('folderId', folderId);
    if (ownerId) formData.append('ownerId', ownerId);
    
    return await api.post<FileEntity>(`${this.basePath}/upload`, formData);
  }

  /**
   * Get files by folder
   */
  public async getFilesByFolder(folderId: string, page: number = 1): Promise<PageDTO<FileEntity>> {
    return await api.get<PageDTO<FileEntity>>(`${this.basePath}/folder/${folderId}?page=${page}`);
  }

  /**
   * Search files by name
   */
  public async searchFiles(name: string, page: number = 1): Promise<PageDTO<FileEntity>> {
    return await api.get<PageDTO<FileEntity>>(`${this.basePath}/search?name=${encodeURIComponent(name)}&page=${page}`);
  }

  /**
   * Soft delete a file
   */
  public async deleteFile(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  /**
   * Restore a soft-deleted file
   */
  public async restoreFile(id: string): Promise<void> {
    await api.put(`${this.basePath}/${id}/restore`, {});
  }
}

const fileService = new FileService();
export default fileService;