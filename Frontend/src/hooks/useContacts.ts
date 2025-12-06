// useContacts.ts
"use client";
import { useState, useEffect, useCallback } from 'react';
import ContactService, { Contact } from '@/services/contactService';
import { PageDTO } from '@/services/userService';

export const useContacts = (page: number = 1) => {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const fetchContacts = useCallback(async () => {
    try {
      setLoading(true);
      const response: PageDTO<Contact> = await ContactService.getAllContacts(page);
      setContacts(response?.context || []);
      setTotalPages(response?.totalPageCount || 0);
      setCurrentPage(response?.currentPage || 1);
      setError(null);
    } catch (err) {
      setError('Failed to fetch contacts');
      setContacts([]);
      console.error('Error fetching contacts:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchContacts();
  }, [fetchContacts]);

  // Return fetchContacts as refetch function
  return { contacts, loading, error, totalPages, currentPage, refetch: fetchContacts };
};

export const useContact = (id: string | null) => {
  const [contact, setContact] = useState<Contact | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchContact = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await ContactService.getContactById(id);
      setContact(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch contact');
      console.error('Error fetching contact:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchContact();
  }, [fetchContact]);

  return { contact, loading, error, refetch: fetchContact };
};