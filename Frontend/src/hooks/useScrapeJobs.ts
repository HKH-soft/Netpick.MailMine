// useScrapeJobs.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import ScrapeJobService , { ScrapeJob } from '@/services/scrapeJobService';
import { PageDTO } from '@/services/api';

export const useScrapeJobs = (page: number = 1) => {
  const [scrapeJobs, setScrapeJobs] = useState<ScrapeJob[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchScrapeJobs = useCallback(async () => {
    try {
      setLoading(true);
      const response: PageDTO<ScrapeJob> = await ScrapeJobService.getAllScrapeJobs(page);
      setScrapeJobs(response.context);
      setTotalPages(response.totalPageCount);
      setCurrentPage(response.currentPage);
      setError(null);
    } catch (err) {
      setError('Failed to fetch scrape jobs');
      console.error('Error fetching scrape jobs:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchScrapeJobs();
  }, [fetchScrapeJobs]);

  // Return fetchScrapeJobs as refetch function
  return { scrapeJobs, loading, error, totalPages, currentPage, refetch: fetchScrapeJobs };
};

export const useScrapeJob = (id: string | null) => {
  const [scrapeJob, setScrapeJob] = useState<ScrapeJob | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchScrapeJob = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await ScrapeJobService.getScrapeJobById(id);
      setScrapeJob(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch scrape job');
      console.error('Error fetching scrape job:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchScrapeJob();
  }, [fetchScrapeJob]);

  return { scrapeJob, loading, error, refetch: fetchScrapeJob };
};