"use client";

import React from 'react';
import ProtectedRoute from '@/components/common/ProtectedRoute';

export default function ProtectedTest() {
  return (
    <ProtectedRoute>
      <div className="p-6">
        <h1 className="text-2xl font-bold mb-4">Protected Test Page</h1>
        <p className="mb-4">If you can see this page, you are authenticated!</p>
        <button 
          onClick={() => window.location.href = '/users'} 
          className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mr-2"
        >
          Go to Users Page
        </button>
        <button 
          onClick={() => window.location.href = '/'} 
          className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded"
        >
          Go Home
        </button>
      </div>
    </ProtectedRoute>
  );
}