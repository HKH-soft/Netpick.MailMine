// useApiKeys.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import ApiKeyService, { ApiKey } from '@/services/apiKeyService';
import { PageDTO } from '@/services/api';

export const useApiKeys = (page: number = 1) => {
  const [apiKeys, setApiKeys] = useState<ApiKey[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchApiKeys = useCallback(async () => {
    try {
      setLoading(true);
      const response: PageDTO<ApiKey> = await ApiKeyService.getAllApiKeys(page);
      setApiKeys(response.context);
      setTotalPages(response.totalPageCount);
      setCurrentPage(response.currentPage);
      setError(null);
    } catch (err) {
      setError('Failed to fetch API keys');
      console.error('Error fetching API keys:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchApiKeys();
  }, [fetchApiKeys]);

  // Return fetchApiKeys as refetch function
  return { apiKeys, loading, error, totalPages, currentPage, refetch: fetchApiKeys };
};

export const useApiKey = (id: string | null) => {
  const [apiKey, setApiKey] = useState<ApiKey | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchApiKey = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await ApiKeyService.getApiKeyById(id);
      setApiKey(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch API key');
      console.error('Error fetching API key:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchApiKey();
  }, [fetchApiKey]);

  return { apiKey, loading, error, refetch: fetchApiKey };
};