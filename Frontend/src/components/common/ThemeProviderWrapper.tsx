"use client";

import React, { useState, useEffect } from 'react';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { useTheme } from '@/context/ThemeContext';

interface ThemeProviderWrapperProps {
  children: React.ReactNode;
}

const ThemeProviderWrapper: React.FC<ThemeProviderWrapperProps> = ({ 
  children 
}) => {
  const { isInitialized } = useTheme();
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  // Render loading state until component is mounted and theme is initialized
  // This prevents hydration errors
  if (!isMounted || !isInitialized) {
    return <LoadingSpinner />;
  }

  return <>{children}</>;
};

export default ThemeProviderWrapper;