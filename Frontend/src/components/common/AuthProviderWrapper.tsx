"use client";

import React, { useState, useEffect } from 'react';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { useAuth } from '@/context/AuthContext';

interface AuthProviderWrapperProps {
  children: React.ReactNode;
}

const AuthProviderWrapper: React.FC<AuthProviderWrapperProps> = ({ 
  children 
}) => {
  const { isInitialized } = useAuth();
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  // Render loading state until component is mounted and auth state is initialized
  // This prevents hydration errors
  if (!isMounted || !isInitialized) {
    return <LoadingSpinner />;
  }

  return <>{children}</>;
};

export default AuthProviderWrapper;