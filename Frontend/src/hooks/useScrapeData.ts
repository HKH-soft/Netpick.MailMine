// useScrapeData.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import ScrapeDataService, { ScrapeData } from '@/services/scrapeDataService';
import { PageDTO } from '@/services/api';


export const useScrapeDataList = (page: number = 1) => {
  const [scrapeDataList, setScrapeDataList] = useState<ScrapeData[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchScrapeData = useCallback(async () => {
    try {
      setLoading(true);
      const response: PageDTO<ScrapeData> = await ScrapeDataService.getAllScrapeData(page);
      setScrapeDataList(response?.context || []);
      setTotalPages(response?.totalPageCount || 0);
      setCurrentPage(response?.currentPage || 1);
      setError(null);
    } catch (err) {
      setError('Failed to fetch scrape data');
      setScrapeDataList([]);
      console.error('Error fetching scrape data:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchScrapeData();
  }, [fetchScrapeData]);

  return { scrapeDataList, loading, error, totalPages, currentPage, refetch: fetchScrapeData };
};

export const useScrapeData = (id: string | null) => {
  const [scrapeData, setScrapeData] = useState<ScrapeData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchScrapeData = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await ScrapeDataService.getScrapeDataById(id);
      setScrapeData(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch scrape data');
      console.error('Error fetching scrape data:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchScrapeData();
  }, [fetchScrapeData]);

  return { scrapeData, loading, error, refetch: fetchScrapeData };
};