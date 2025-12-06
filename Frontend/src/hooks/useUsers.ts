// useUsers.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import UserService, { User, PageDTO } from '@/services/userService';

export const useUsers = (page: number = 1) => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      const response: PageDTO<User> = await UserService.getAllUsers(page);
      setUsers(response.context);
      setTotalPages(response.totalPageCount);
      setCurrentPage(response.currentPage);
      setError(null);
    } catch (err) {
      setError('Failed to fetch users');
      console.error('Error fetching users:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  // Return fetchUsers as refetch function
  return { users, loading, error, totalPages, currentPage, refetch: fetchUsers };
};

export const useUser = (userId: string | null) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUser = useCallback(async () => {
    if (!userId) return;

    try {
      setLoading(true);
      const data = await UserService.getUserById(userId);
      setUser(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch user');
      console.error('Error fetching user:', err);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  return { user, loading, error, refetch: fetchUser };
};