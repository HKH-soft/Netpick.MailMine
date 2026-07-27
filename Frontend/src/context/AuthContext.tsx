// AuthContext.tsx
"use client";

import React, { createContext, useContext, useState, useEffect, useCallback, useMemo, ReactNode } from 'react';
import AuthService from '@/services/authService';

interface AuthContextType {
  isAuthenticated: boolean;
  login: () => void;
  logout: () => void;
  isInitialized: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Check if dev mode is enabled (mock data, no auth required)
const isDevMode = process.env.NEXT_PUBLIC_DEV_MODE === 'true';

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isInitialized, setIsInitialized] = useState<boolean>(false);

  useEffect(() => {
    if (isDevMode) {
      setIsAuthenticated(true);
      setIsInitialized(true);
      return;
    }

    setIsAuthenticated(AuthService.isAuthenticated());
    setIsInitialized(true);
  }, []);

  const login = useCallback(() => {
    setIsAuthenticated(true);
  }, []);

  const logout = useCallback(() => {
    if (!isDevMode) {
      AuthService.removeToken();
    }
    setIsAuthenticated(false);
  }, []);

  if (!isInitialized && !isDevMode) {
    return null;
  }

  const value = useMemo(
    () => ({ isAuthenticated, login, logout, isInitialized }),
    [isAuthenticated, login, logout, isInitialized]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};


