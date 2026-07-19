"use client";

import React from 'react';
import { ExtensionProvider } from '@/context/ExtensionContext';

interface ExtensionProviderWrapperProps {
  children: React.ReactNode;
}

const ExtensionProviderWrapper: React.FC<ExtensionProviderWrapperProps> = ({ children }) => {
  return <ExtensionProvider>{children}</ExtensionProvider>;
};

export default ExtensionProviderWrapper;