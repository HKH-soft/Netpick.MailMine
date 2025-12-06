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

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isInitialized, setIsInitialized] = useState<boolean>(false);

  useEffect(() => {
    // Check if user is authenticated on initial load and validate token
    setIsAuthenticated(AuthService.isAuthenticated());
    setIsInitialized(true);
  }, []);

  const login = () => {
    // Note: AuthService.setToken is already called in the signin method
    setIsAuthenticated(true);
  };

  const logout = () => {
    AuthService.removeToken();
    setIsAuthenticated(false);
  };

  // Don't render children until auth state is initialized to prevent hydration errors
  if (!isInitialized) {
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