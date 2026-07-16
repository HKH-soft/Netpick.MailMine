"use client";
import { useState, useEffect, useCallback } from 'react';
import emailMessageService, { EmailMessage } from '@/services/emailMessageService';

export const useScrapeMessages = (page: number = 0) => {
  const [messages, setMessages] = useState<EmailMessage[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [currentPage, setCurrentPage] = useState<number>(0);

  const fetchMessages = useCallback(async () => {
    try {
      setLoading(true);
      const data = await emailMessageService.listEmails(page);
      setMessages(data || []);
      setTotalPages(Math.max(1, Math.ceil((data?.length || 0) / 20)));
      setCurrentPage(page);
      setError(null);
    } catch (err) {
      setError('Failed to fetch messages');
      setMessages([]);
      console.error('Error fetching messages:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchMessages();
  }, [fetchMessages]);

  return { messages, loading, error, totalPages, currentPage, refetch: fetchMessages };
};
