"use client";

import React, { useEffect, useState } from "react";
import AuthService from "@/services/authService";

export default function TokenDebugPage() {
  const [tokenInfo, setTokenInfo] = useState<{ token: string; payload: Record<string, unknown> } | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    try {
      const token = AuthService.getToken();
      
      if (!token) {
        setError("No token found");
        return;
      }

      // Parse JWT token
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      
      const payload = JSON.parse(jsonPayload);
      
      setTokenInfo({
        token: token,
        payload: payload
      });
    } catch (err) {
      setError("Error parsing token: " + (err as Error).message);
    }
  }, []);

  if (error) {
    return (
      <div className="p-6">
        <h1 className="text-2xl font-bold mb-4">Token Debug Page</h1>
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          <p>Error: {error}</p>
        </div>
      </div>
    );
  }

  if (!tokenInfo) {
    return (
      <div className="p-6">
        <h1 className="text-2xl font-bold mb-4">Token Debug Page</h1>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Token Debug Page</h1>
      
      <div className="mb-6">
        <h2 className="text-xl font-semibold mb-2">Token Information</h2>
        <div className="bg-gray-100 p-4 rounded">
          <p><strong>Role:</strong> {tokenInfo.payload.role ? String(tokenInfo.payload.role) : "No role found"}</p>
          <p><strong>Email:</strong> {tokenInfo.payload.email ? String(tokenInfo.payload.email) : "No email found"}</p>
          <p><strong>User ID:</strong> {tokenInfo.payload.id ? String(tokenInfo.payload.id) : "No ID found"}</p>
          <p><strong>Expiration:</strong> {tokenInfo.payload.exp ? new Date(Number(tokenInfo.payload.exp) * 1000).toLocaleString() : "No expiration"}</p>
        </div>
      </div>

      <div>
        <h2 className="text-xl font-semibold mb-2">Full Token Payload</h2>
        <pre className="bg-gray-100 p-4 rounded overflow-auto max-h-96">
          {JSON.stringify(tokenInfo.payload, null, 2)}
        </pre>
      </div>
    </div>
  );
}