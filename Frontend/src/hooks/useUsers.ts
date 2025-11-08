// useUsers.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import UserService, { User, AllUsersResponse } from '@/services/userService';

export const useUsers = (page: number = 1) => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      const response: AllUsersResponse = await UserService.getAllUsers(page);
      setUsers(response.users);
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

export const useUserProfile = (token: string | null) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUserProfile = useCallback(async () => {
    if (!token) return;

    try {
      setLoading(true);
      const data = await UserService.getUserProfile();
      setUser(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch user profile');
      console.error('Error fetching user profile:', err);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    fetchUserProfile();
  }, [fetchUserProfile]);

  return { user, loading, error, refetch: fetchUserProfile };
};