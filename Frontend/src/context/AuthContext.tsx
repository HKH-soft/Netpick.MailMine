// AuthContext.tsx
"use client";

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
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
    // In dev mode, skip authentication check and auto-authenticate
    if (isDevMode) {
      setIsAuthenticated(true);
      setIsInitialized(true);
      return;
    }

    // Check if user is authenticated on initial load and validate token
    setIsAuthenticated(AuthService.isAuthenticated());
    setIsInitialized(true);
  }, []);

  const login = () => {
    // Note: AuthService.setToken is already called in the signin method
    setIsAuthenticated(true);
  };

  const logout = () => {
    if (!isDevMode) {
      AuthService.removeToken();
    }
    setIsAuthenticated(false);
  };

  // In dev mode, render children immediately without waiting for auth initialization
  if (!isInitialized && !isDevMode) {
    return null;
  }

  const value = {
    isAuthenticated,
    login,
    logout,
    isInitialized
  };

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


