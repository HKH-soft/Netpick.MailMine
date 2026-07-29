"use client";

import React, { useState, useEffect } from 'react';
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

  // Render minimal skeleton until mounted + theme initialized to avoid layout shift
  if (!isMounted || !isInitialized) {
    return (
      <div className="min-h-screen animate-pulse bg-gray-100 dark:bg-gray-900" aria-busy="true" aria-label="Loading theme" />
    );
  }

  return <>{children}</>;
};

export default ThemeProviderWrapper;