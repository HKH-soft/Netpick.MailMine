// useProxies.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import ProxyService, { Proxy, ProxyStats } from '@/services/proxyService';
import { PageDTO } from '@/services/api';

export const useProxies = (page: number = 1) => {
  const [proxies, setProxies] = useState<Proxy[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchProxies = useCallback(async () => {
    try {
      setLoading(true);
      const response: PageDTO<Proxy> = await ProxyService.getAllProxies(page);
      setProxies(response.context);
      setTotalPages(response.totalPageCount);
      setCurrentPage(response.currentPage);
      setError(null);
    } catch (err) {
      setError('Failed to fetch proxies');
      console.error('Error fetching proxies:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchProxies();
  }, [fetchProxies]);

  return { proxies, loading, error, totalPages, currentPage, refetch: fetchProxies };
};

export const useProxy = (id: string | null) => {
  const [proxy, setProxy] = useState<Proxy | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProxy = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await ProxyService.getProxyById(id);
      setProxy(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch proxy');
      console.error('Error fetching proxy:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchProxy();
  }, [fetchProxy]);

  return { proxy, loading, error, refetch: fetchProxy };
};

export const useProxyStats = () => {
  const [stats, setStats] = useState<ProxyStats | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStats = useCallback(async () => {
    try {
      setLoading(true);
      const data = await ProxyService.getStats();
      setStats(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch proxy stats');
      console.error('Error fetching proxy stats:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  return { stats, loading, error, refetch: fetchStats };
};
