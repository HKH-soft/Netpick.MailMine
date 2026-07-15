// useTasks.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import TaskService, { Task } from '@/services/taskService';
import { PageDTO } from '@/services/api';

export const useTasks = (page: number = 1, filters?: { status?: string; projectId?: string }) => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchTasks = useCallback(async () => {
    try {
      setLoading(true);
      let response: PageDTO<Task>;
      if (filters?.status) {
        response = await TaskService.getTasksByStatus(filters.status, page);
      } else if (filters?.projectId) {
        response = await TaskService.getTasksByProject(filters.projectId, page);
      } else {
        response = await TaskService.getAllTasks(page);
      }
      setTasks(response?.context || []);
      setTotalPages(response?.totalPageCount || 0);
      setCurrentPage(response?.currentPage || 1);
      setError(null);
    } catch (err) {
      setError('Failed to fetch tasks');
      setTasks([]);
      console.error('Error fetching tasks:', err);
    } finally {
      setLoading(false);
    }
  }, [page, filters?.status, filters?.projectId]);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  return { tasks, loading, error, totalPages, currentPage, refetch: fetchTasks };
};

export const useTask = (id: string | null) => {
  const [task, setTask] = useState<Task | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTask = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await TaskService.getTaskById(id);
      setTask(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch task');
      console.error('Error fetching task:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchTask();
  }, [fetchTask]);

  return { task, loading, error, refetch: fetchTask };
};