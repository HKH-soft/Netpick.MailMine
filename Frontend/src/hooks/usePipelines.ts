// usePipelines.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import PipelineService, { Pipeline } from '@/services/pipelineService';
import { PageDTO } from '@/services/api';

export const usePipelines = (page: number = 1) => {
  const [pipelines, setPipelines] = useState<Pipeline[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchPipelines = useCallback(async () => {
    try {
      setLoading(true);
      const response: PageDTO<Pipeline> = await PipelineService.getAllPipelines(page);
      setPipelines(response.context);
      setTotalPages(response.totalPageCount);
      setCurrentPage(response.currentPage);
      setError(null);
    } catch (err) {
      setError('Failed to fetch pipelines');
      console.error('Error fetching pipelines:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchPipelines();
  }, [fetchPipelines]);

  return { pipelines, loading, error, totalPages, currentPage, refetch: fetchPipelines };
};

export const usePipeline = (id: string | null) => {
  const [pipeline, setPipeline] = useState<Pipeline | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPipeline = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await PipelineService.getPipelineById(id);
      setPipeline(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch pipeline');
      console.error('Error fetching pipeline:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchPipeline();
  }, [fetchPipeline]);

  return { pipeline, loading, error, refetch: fetchPipeline };
};
