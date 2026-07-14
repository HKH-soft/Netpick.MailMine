"use client";

import React, { useEffect, useState } from 'react';
import '@/i18n/config';

interface I18nProviderWrapperProps {
  children: React.ReactNode;
}

const I18nProviderWrapper: React.FC<I18nProviderWrapperProps> = ({ children }) => {
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  if (!isMounted) {
    return <>{children}</>;
  }

  return <>{children}</>;
};

export default I18nProviderWrapper;