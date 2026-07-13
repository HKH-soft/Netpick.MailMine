// GlobalSearchPanel.tsx
"use client";
import React, { useState, useCallback } from 'react';
import searchService, { SearchResult, GlobalSearchResponse } from '@/services/searchService';

export default function GlobalSearchPanel() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<GlobalSearchResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);

  const handleSearch = useCallback(async () => {
    if (!query.trim() || query.length < 2) {
      setResults(null);
      return;
    }
    try {
      setLoading(true);
      const data = await searchService.search(query);
      setResults(data);
      setIsOpen(true);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  }, [query]);

  const renderResultItem = (item: SearchResult) => {
    const typeIcon: Record<string, string> = {
      email: '📧',
      tag: '🏷️',
      sharedInbox: '📥',
      campaign: '📢',
    };
    return (
      <div
        key={`${item.type}-${item.id}`}
        className="flex items-center gap-3 p-3 hover:bg-gray-50 dark:hover:bg-meta-4 cursor-pointer border-b border-[#eee] dark:border-strokedark"
        onClick={() => setIsOpen(false)}
      >
        <span className="text-lg">{typeIcon[item.type] || '📄'}</span>
        <div className="flex-1 min-w-0">
          <p className="font-medium text-black dark:text-white truncate">{item.title}</p>
          <p className="text-sm text-gray-500 truncate">{item.subtitle}</p>
        </div>
        <span className="text-xs bg-gray-100 dark:bg-meta-4 px-2 py-1 rounded">{item.type}</span>
      </div>
    );
  };

  return (
    <div className="relative">
      <div className="flex items-center gap-2">
        <input
          type="text"
          placeholder="Search emails, tags, inboxes, campaigns..."
          value={query}
          onChange={e => setQuery(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleSearch()}
          className="w-64 border rounded-lg px-3 py-2 text-sm dark:bg-form-input dark:border-form-strokedark"
        />
        <button
          onClick={handleSearch}
          disabled={loading}
          className="bg-primary text-white px-3 py-2 rounded-lg hover:bg-opacity-90 text-sm"
        >
          {loading ? '...' : '🔍'}
        </button>
      </div>

      {isOpen && results && (
        <div className="absolute top-full left-0 right-0 mt-2 bg-white dark:bg-boxdark rounded-lg shadow-lg border border-stroke z-50 max-h-96 overflow-y-auto">
          <div className="p-2 border-b border-stroke">
            <div className="flex justify-between items-center">
              <span className="text-sm font-medium text-black dark:text-white">
                {results.totalResults} result(s)
              </span>
              <button onClick={() => setIsOpen(false)} className="text-gray-500 hover:text-black">✕</button>
            </div>
          </div>
          {results.emails.length > 0 && (
            <div>
              <p className="px-3 py-1 text-xs font-medium text-gray-500 uppercase">Emails</p>
              {results.emails.map(renderResultItem)}
            </div>
          )}
          {results.tags.length > 0 && (
            <div>
              <p className="px-3 py-1 text-xs font-medium text-gray-500 uppercase">Tags</p>
              {results.tags.map(renderResultItem)}
            </div>
          )}
          {results.sharedInboxes.length > 0 && (
            <div>
              <p className="px-3 py-1 text-xs font-medium text-gray-500 uppercase">Shared Inboxes</p>
              {results.sharedInboxes.map(renderResultItem)}
            </div>
          )}
          {results.campaigns.length > 0 && (
            <div>
              <p className="px-3 py-1 text-xs font-medium text-gray-500 uppercase">Campaigns</p>
              {results.campaigns.map(renderResultItem)}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
