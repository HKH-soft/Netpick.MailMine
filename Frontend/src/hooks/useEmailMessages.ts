// useEmailMessages.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import emailMessageService, { EmailMessage } from '@/services/emailMessageService';

export const useEmailMessages = (page: number = 0) => {
  const [emails, setEmails] = useState<EmailMessage[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchEmails = useCallback(async (signal?: AbortSignal) => {
    try {
      setLoading(true);
      const data = await emailMessageService.listEmails(page);
      setEmails(data);
      setError(null);
    } catch (err: unknown) {
      if ((err as { name?: string })?.name === 'AbortError') return;
      setError('Failed to fetch emails');
      setEmails([]);
      console.error('Error fetching emails:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    const controller = new AbortController();
    fetchEmails(controller.signal);
    return () => controller.abort();
  }, [fetchEmails]);

  return { emails, loading, error, refetch: fetchEmails };
};

export const useEmailMessage = (id: string | null) => {
  const [email, setEmail] = useState<EmailMessage | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchEmail = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await emailMessageService.getEmail(id);
      setEmail(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch email');
      console.error('Error fetching email:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchEmail();
  }, [fetchEmail]);

  return { email, loading, error, refetch: fetchEmail };
};