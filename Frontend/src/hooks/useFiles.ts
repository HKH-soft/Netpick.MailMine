// useFiles.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import FileService, { FileEntity } from '@/services/fileService';
import { PageDTO } from '@/services/api';

export const useFiles = (page: number = 1, folderId?: string) => {
  const [files, setFiles] = useState<FileEntity[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchFiles = useCallback(async () => {
    try {
      setLoading(true);
      let response: PageDTO<FileEntity>;
      if (folderId) {
        response = await FileService.getFilesByFolder(folderId, page);
      } else {
        response = await FileService.getAllFiles(page);
      }
setFiles(response?.content || []);
       setTotalPages(response?.totalPages || 0);
      setCurrentPage(response?.currentPage || 1);
      setError(null);
    } catch (err) {
      setError('Failed to fetch files');
      setFiles([]);
      console.error('Error fetching files:', err);
    } finally {
      setLoading(false);
    }
  }, [page, folderId]);

  useEffect(() => {
    fetchFiles();
  }, [fetchFiles]);

  return { files, loading, error, totalPages, currentPage, refetch: fetchFiles };
};

export const useFile = (id: string | null) => {
  const [file, setFile] = useState<FileEntity | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchFile = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await FileService.getFileById(id);
      setFile(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch file');
      console.error('Error fetching file:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchFile();
  }, [fetchFile]);

  return { file, loading, error, refetch: fetchFile };
};