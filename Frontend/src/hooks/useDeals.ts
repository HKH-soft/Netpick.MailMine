// useDeals.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import DealService, { Deal, DealStats } from '@/services/dealService';
import { PageDTO } from '@/services/api';

export const useDeals = (page: number = 1) => {
  const [deals, setDeals] = useState<Deal[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchDeals = useCallback(async () => {
    try {
      setLoading(true);
      const response: PageDTO<Deal> = await DealService.getAllDeals(page);
      setDeals(response?.context || []);
      setTotalPages(response?.totalPageCount || 0);
      setCurrentPage(response?.currentPage || 1);
      setError(null);
    } catch (err) {
      setError('Failed to fetch deals');
      setDeals([]);
      console.error('Error fetching deals:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchDeals();
  }, [fetchDeals]);

  return { deals, loading, error, totalPages, currentPage, refetch: fetchDeals };
};

export const useDeal = (id: string | null) => {
  const [deal, setDeal] = useState<Deal | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDeal = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await DealService.getDealById(id);
      setDeal(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch deal');
      console.error('Error fetching deal:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchDeal();
  }, [fetchDeal]);

  return { deal, loading, error, refetch: fetchDeal };
};

export const useDealStats = () => {
  const [stats, setStats] = useState<DealStats | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStats = useCallback(async () => {
    try {
      setLoading(true);
      const data = await DealService.getStats();
      setStats(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch deal stats');
      console.error('Error fetching deal stats:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  return { stats, loading, error, refetch: fetchStats };
};