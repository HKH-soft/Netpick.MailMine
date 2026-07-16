// useTransactions.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import TransactionService, { Transaction } from '@/services/transactionService';
import { PageDTO } from '@/services/api';

export const useTransactions = (page: number = 1) => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchTransactions = useCallback(async () => {
    try {
      setLoading(true);
      const response: PageDTO<Transaction> = await TransactionService.getAllTransactions(page);
      setTransactions(response?.content || []);
      setTotalPages(response?.totalPages || 0);
      setCurrentPage(response?.currentPage || 1);
      setError(null);
    } catch (err) {
      setError('Failed to fetch transactions');
      setTransactions([]);
      console.error('Error fetching transactions:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchTransactions();
  }, [fetchTransactions]);

  return { transactions, loading, error, totalPages, currentPage, refetch: fetchTransactions };
};

export const useTransaction = (id: string | null) => {
  const [transaction, setTransaction] = useState<Transaction | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTransaction = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await TransactionService.getTransactionById(id);
      setTransaction(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch transaction');
      console.error('Error fetching transaction:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchTransaction();
  }, [fetchTransaction]);

  return { transaction, loading, error, refetch: fetchTransaction };
};