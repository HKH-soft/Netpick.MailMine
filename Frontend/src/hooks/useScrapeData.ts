// useScrapeData.ts
"use client";
import { useState, useEffect } from 'react';
import ScrapeDataService, { ScrapeData } from '@/services/scrapeDataService';
import { ApiResponse } from '@/services/api';


export const useScrapeDataList = (page: number = 1) => {
  const [scrapeDataList, setScrapeDataList] = useState<ScrapeData[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [totalElements, setTotalElements] = useState<number>(0);

  useEffect(() => {
    const fetchScrapeData = async () => {
      try {
        setLoading(true);
        const response: ApiResponse<ScrapeData[]> = await ScrapeDataService.getAllScrapeData(page);
        setScrapeDataList(response.content);
        setTotalPages(response.totalPages);
        setTotalElements(response.totalElements);
        setError(null);
      } catch (err) {
        setError('Failed to fetch scrape data');
        console.error('Error fetching scrape data:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchScrapeData();
  }, [page]);

  return { scrapeDataList, loading, error, totalPages, totalElements };
};

export const useScrapeData = (id: string | null) => {
  const [scrapeData, setScrapeData] = useState<ScrapeData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    const fetchScrapeData = async () => {
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
    };

    fetchScrapeData();
  }, [id]);

  return { scrapeData, loading, error };
};