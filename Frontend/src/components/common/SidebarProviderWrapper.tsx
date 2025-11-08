"use client";

import React, { useState, useEffect } from 'react';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { useSidebar } from '@/context/SidebarContext';

interface SidebarProviderWrapperProps {
  children: React.ReactNode;
}

const SidebarProviderWrapper: React.FC<SidebarProviderWrapperProps> = ({ 
  children 
}) => {
  const { isInitialized } = useSidebar();
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  // Render loading state until component is mounted and sidebar state is initialized
  // This prevents hydration errors
  if (!isMounted || !isInitialized) {
    return <LoadingSpinner />;
  }

  return <>{children}</>;
};

export default SidebarProviderWrapper;