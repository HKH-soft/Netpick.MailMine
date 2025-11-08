"use client";
import React from "react";

const LoadingSpinner: React.FC = () => {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-white dark:bg-gray-900">
      <div className="h-16 w-16 animate-spin rounded-full border-4 border-solid border-brand-500 border-t-transparent dark:border-blue-500"></div>
    </div>
  );
};

export default LoadingSpinner;