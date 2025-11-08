"use client";

import React, { useEffect, useState } from 'react';

interface ApiKey {
  id: number;
  key: string;
  description: string | null;
}

interface SearchQuery {
  id: number;
  sentence: string;
  link_count: number;
}

export default function ApiTest() {
  const [apiKeys, setApiKeys] = useState<ApiKey[]>([]);
  const [searchQueries, setSearchQueries] = useState<SearchQuery[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Test API Keys endpoint
        const apiKeyResponse = await fetch('http://localhost:8080/apikey?page=1');
        if (apiKeyResponse.ok) {
          const apiKeyData = await apiKeyResponse.json();
          setApiKeys(apiKeyData.content || []);
        } else {
          console.error('Failed to fetch API keys:', apiKeyResponse.status);
        }
        
        // Test Search Queries endpoint
        const searchQueryResponse = await fetch('http://localhost:8080/search_query?page=1');
        if (searchQueryResponse.ok) {
          const searchQueryData = await searchQueryResponse.json();
          setSearchQueries(searchQueryData.content || []);
        } else {
          console.error('Failed to fetch search queries:', searchQueryResponse.status);
        }
        
        setError(null);
      } catch (err) {
        setError('Failed to fetch data');
        console.error('Error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return <div className="p-6">Loading...</div>;
  }

  if (error) {
    return <div className="p-6 text-red-500">Error: {error}</div>;
  }

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">API Test</h1>
      
      <div className="mb-8">
        <h2 className="text-xl font-semibold mb-2">API Keys</h2>
        {apiKeys.length > 0 ? (
          <ul className="list-disc pl-5">
            {apiKeys.map((key) => (
              <li key={key.id}>{key.key} - {key.description || 'No description'}</li>
            ))}
          </ul>
        ) : (
          <p>No API keys found</p>
        )}
      </div>
      
      <div>
        <h2 className="text-xl font-semibold mb-2">Search Queries</h2>
        {searchQueries.length > 0 ? (
          <ul className="list-disc pl-5">
            {searchQueries.map((query) => (
              <li key={query.id}>{query.sentence} - {query.link_count} results</li>
            ))}
          </ul>
        ) : (
          <p>No search queries found</p>
        )}
      </div>
    </div>
  );
}