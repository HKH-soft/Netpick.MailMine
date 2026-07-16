// useInvoices.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import InvoiceService, { Invoice, FinanceSummary } from '@/services/invoiceService';
import { PageDTO } from '@/services/api';

export const useInvoices = (page: number = 1) => {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchInvoices = useCallback(async () => {
    try {
      setLoading(true);
      const response: PageDTO<Invoice> = await InvoiceService.getAllInvoices(page);
      setInvoices(response?.content || []);
      setTotalPages(response?.totalPages || 0);
      setCurrentPage(response?.currentPage || 1);
      setError(null);
    } catch (err) {
      setError('Failed to fetch invoices');
      setInvoices([]);
      console.error('Error fetching invoices:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchInvoices();
  }, [fetchInvoices]);

  return { invoices, loading, error, totalPages, currentPage, refetch: fetchInvoices };
};

export const useInvoice = (id: string | null) => {
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchInvoice = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await InvoiceService.getInvoiceById(id);
      setInvoice(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch invoice');
      console.error('Error fetching invoice:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchInvoice();
  }, [fetchInvoice]);

  return { invoice, loading, error, refetch: fetchInvoice };
};

export const useFinanceSummary = () => {
  const [summary, setSummary] = useState<FinanceSummary | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSummary = useCallback(async () => {
    try {
      setLoading(true);
      const data = await InvoiceService.getFinanceSummary();
      setSummary(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch finance summary');
      console.error('Error fetching finance summary:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSummary();
  }, [fetchSummary]);

  return { summary, loading, error, refetch: fetchSummary };
};