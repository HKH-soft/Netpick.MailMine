// useFolders.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import FolderService, { Folder } from '@/services/folderService';
import { PageDTO } from '@/services/api';

export const useFolders = (page: number = 1, ownerId?: string) => {
  const [folders, setFolders] = useState<Folder[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchFolders = useCallback(async () => {
    try {
      setLoading(true);
      let response: PageDTO<Folder> | Folder[];
      if (ownerId) {
        response = await FolderService.getFoldersByOwner(ownerId);
        setFolders(response as Folder[]);
        setTotalPages(1);
        setCurrentPage(1);
      } else {
        response = await FolderService.getAllFolders(page);
setFolders(response?.content || []);
         setTotalPages(response?.totalPages || 0);
         setCurrentPage(response?.currentPage || 1);
      }
      setError(null);
    } catch (err) {
      setError('Failed to fetch folders');
      setFolders([]);
      console.error('Error fetching folders:', err);
    } finally {
      setLoading(false);
    }
  }, [page, ownerId]);

  useEffect(() => {
    fetchFolders();
  }, [fetchFolders]);

  return { folders, loading, error, totalPages, currentPage, refetch: fetchFolders };
};

export const useFolder = (id: string | null) => {
  const [folder, setFolder] = useState<Folder | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchFolder = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await FolderService.getFolderById(id);
      setFolder(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch folder');
      console.error('Error fetching folder:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchFolder();
  }, [fetchFolder]);

  return { folder, loading, error, refetch: fetchFolder };
};