"use client";

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import AuthService from '@/services/authService';
import Link from 'next/link';

export default function AuthTest() {
  const { isAuthenticated, logout } = useAuth();
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    setToken(AuthService.getToken());
  }, [isAuthenticated]);

  const handleLogout = () => {
    logout();
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Authentication Test</h1>
      
      <div className="mb-4">
        <p className="mb-2">
          <span className="font-semibold">Authenticated:</span> {isAuthenticated ? 'Yes' : 'No'}
        </p>
        <p className="mb-2">
          <span className="font-semibold">Token:</span> {token ? 'Present' : 'Not present'}
        </p>
        <p>
          <span className="font-semibold">Token valid:</span> {AuthService.isAuthenticated() ? 'Yes' : 'No'}
        </p>
      </div>
      
      {isAuthenticated ? (
        <button 
          onClick={handleLogout}
          className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
        >
          Logout
        </button>
      ) : (
        <div className="space-x-4">
          <Link href="/signin" className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600">
            Go to Sign In
          </Link>
          <Link href="/signup" className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600">
            Go to Sign Up
          </Link>
        </div>
      )}
    </div>
  );
}