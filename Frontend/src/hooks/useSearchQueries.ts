// useSearchQueries.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import SearchQueryService, { SearchQuery } from '@/services/searchQueryService';
import { PageDTO } from '@/services/api';

export const useSearchQueries = (page: number = 1) => {
  const [searchQueries, setSearchQueries] = useState<SearchQuery[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchSearchQueries = useCallback(async () => {
    try {
      setLoading(true);
      const response: PageDTO<SearchQuery> = await SearchQueryService.getAllSearchQueries(page);
      setSearchQueries(response.context);
      setTotalPages(response.totalPageCount);
      setCurrentPage(response.currentPage);
      setError(null);
    } catch (err) {
      setError('Failed to fetch search queries');
      console.error('Error fetching search queries:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchSearchQueries();
  }, [fetchSearchQueries]);

  // Return fetchSearchQueries as refetch function
  return { searchQueries, loading, error, totalPages, currentPage, refetch: fetchSearchQueries };
};

export const useSearchQuery = (id: string | null) => {
  const [searchQuery, setSearchQuery] = useState<SearchQuery | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSearchQuery = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await SearchQueryService.getSearchQueryById(id);
      setSearchQuery(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch search query');
      console.error('Error fetching search query:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchSearchQuery();
  }, [fetchSearchQuery]);

  return { searchQuery, loading, error, refetch: fetchSearchQuery };
};