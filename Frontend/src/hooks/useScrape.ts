// useScrape.ts
"use client";
import { useState, useCallback } from 'react';
import ScrapeService, { PipelineStatusResponse, Pipeline, PipelineStageEnum } from '@/services/scrapeService';

export const useScrapeControls = () => {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<PipelineStatusResponse | null>(null);

  const startGoogleSearch = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await ScrapeService.startGoogleSearch();
      return result;
    } catch (err) {
      setError('Failed to start Google search');
      console.error('Error starting Google search:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const startScraping = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await ScrapeService.startScraping();
      return result;
    } catch (err) {
      setError('Failed to start scraping');
      console.error('Error starting scraping:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const startExtract = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await ScrapeService.startExtract();
      return result;
    } catch (err) {
      setError('Failed to start extraction');
      console.error('Error starting extraction:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const executeSteps = useCallback(async (steps: PipelineStageEnum[]) => {
    try {
      setLoading(true);
      setError(null);
      const result = await ScrapeService.executeSteps(steps);
      return result;
    } catch (err) {
      setError('Failed to execute steps');
      console.error('Error executing steps:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const executeAll = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await ScrapeService.executeAll();
      return result;
    } catch (err) {
      setError('Failed to execute all steps');
      console.error('Error executing all steps:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const pausePipeline = useCallback(async (): Promise<Pipeline> => {
    try {
      setLoading(true);
      setError(null);
      const result = await ScrapeService.pausePipeline();
      return result;
    } catch (err) {
      setError('Failed to pause pipeline');
      console.error('Error pausing pipeline:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const resumePipeline = useCallback(async (): Promise<Pipeline> => {
    try {
      setLoading(true);
      setError(null);
      const result = await ScrapeService.resumePipeline();
      return result;
    } catch (err) {
      setError('Failed to resume pipeline');
      console.error('Error resuming pipeline:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const skipCurrentStep = useCallback(async (): Promise<Pipeline> => {
    try {
      setLoading(true);
      setError(null);
      const result = await ScrapeService.skipCurrentStep();
      return result;
    } catch (err) {
      setError('Failed to skip current step');
      console.error('Error skipping current step:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const cancelPipeline = useCallback(async (): Promise<Pipeline> => {
    try {
      setLoading(true);
      setError(null);
      const result = await ScrapeService.cancelPipeline();
      return result;
    } catch (err) {
      setError('Failed to cancel pipeline');
      console.error('Error canceling pipeline:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchStatus = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await ScrapeService.getPipelineStatus();
      setStatus(result);
      return result;
    } catch (err) {
      setError('Failed to fetch pipeline status');
      console.error('Error fetching pipeline status:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    loading,
    error,
    status,
    startGoogleSearch,
    startScraping,
    startExtract,
    executeSteps,
    executeAll,
    pausePipeline,
    resumePipeline,
    skipCurrentStep,
    cancelPipeline,
    fetchStatus,
  };
};
