// useCurrentUser.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import UserService, { User, UserUpdateRequest, PasswordChangeRequest } from '@/services/userService';

export const useCurrentUser = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchCurrentUser = useCallback(async () => {
    try {
      setLoading(true);
      const data = await UserService.getCurrentUser();
      setUser(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch current user');
      console.error('Error fetching current user:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCurrentUser();
  }, [fetchCurrentUser]);

  const updateCurrentUser = useCallback(async (request: UserUpdateRequest) => {
    try {
      setLoading(true);
      const updatedUser = await UserService.updateCurrentUser(request);
      setUser(updatedUser);
      setError(null);
      return updatedUser;
    } catch (err) {
      setError('Failed to update user');
      console.error('Error updating user:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const changePassword = useCallback(async (request: PasswordChangeRequest) => {
    try {
      setLoading(true);
      const result = await UserService.changePassword(request);
      setError(null);
      return result;
    } catch (err) {
      setError('Failed to change password');
      console.error('Error changing password:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const deleteCurrentUser = useCallback(async () => {
    try {
      setLoading(true);
      await UserService.deleteCurrentUser();
      setUser(null);
      setError(null);
    } catch (err) {
      setError('Failed to delete user');
      console.error('Error deleting user:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    user,
    loading,
    error,
    refetch: fetchCurrentUser,
    updateCurrentUser,
    changePassword,
    deleteCurrentUser,
  };
};
