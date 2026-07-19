"use client";
import React, { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";

interface PaletteItem {
  id: string;
  name: string;
  category: string;
  shortcut?: string;
  href?: string;
}

const items: PaletteItem[] = [
  { id: "nav-dashboard", name: "Dashboard", category: "Navigation", href: "/dashboard", shortcut: "G D" },
  { id: "nav-scraping", name: "Scraping", category: "Navigation", href: "/scrape/control", shortcut: "G S" },
  { id: "nav-contacts", name: "Contacts", category: "Navigation", href: "/scrape/contacts" },
  { id: "nav-pipelines", name: "Pipelines", category: "Navigation", href: "/scrape/pipelines" },
  { id: "nav-analytics", name: "Analytics", category: "Navigation", href: "/analytics" },
  { id: "nav-settings", name: "Settings", category: "Navigation", href: "/settings" },
  { id: "nav-api-keys", name: "API Keys", category: "Navigation", href: "/scrape/api-keys" },
  { id: "nav-profile", name: "Profile", category: "Navigation", href: "/profile" },
  { id: "act-create-pipeline", name: "Create Pipeline", category: "Actions", href: "/scrape/pipelines" },
  { id: "act-run-scrape", name: "Run Scrape", category: "Actions", href: "/scrape/control" },
  { id: "act-export", name: "Export Data", category: "Actions" },
  { id: "act-theme", name: "Toggle Theme", category: "Actions" },
];

export default function CommandPalette() {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const router = useRouter();

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault();
        setOpen(prev => !prev);
      }
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, []);

  useEffect(() => {
    if (open) {
      setQuery("");
      setSelectedIndex(0);
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  }, [open]);

  const filtered = items.filter(item =>
    item.name.toLowerCase().includes(query.toLowerCase()) ||
    item.category.toLowerCase().includes(query.toLowerCase())
  );

  useEffect(() => { setSelectedIndex(0); }, [query]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setSelectedIndex(prev => Math.min(prev + 1, filtered.length - 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setSelectedIndex(prev => Math.max(prev - 1, 0));
    } else if (e.key === "Enter" && filtered[selectedIndex]) {
      const item = filtered[selectedIndex];
      if (item.href) { setOpen(false); router.push(item.href); }
    } else if (e.key === "Escape") {
      setOpen(false);
    }
  };

  if (!open) return null;

  const grouped = filtered.reduce<Record<string, PaletteItem[]>>((acc, item) => {
    (acc[item.category] = acc[item.category] || []).push(item);
    return acc;
  }, {});

  return (
    <div className="fixed inset-0 z-[9999] flex items-start justify-center pt-[15vh]" onClick={() => setOpen(false)}>
      <div className="fixed inset-0 bg-black/60 backdrop-blur-sm dark:bg-black/60" />
      <div
        className="relative w-full max-w-lg rounded-2xl border border-gray-200 dark:border-gray-800 overflow-hidden bg-white dark:bg-gray-950 shadow-xl dark:shadow-2xl"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center gap-3 px-4 py-3 border-b border-gray-200 dark:border-gray-800">
          <svg className="w-4 h-4 text-gray-400 dark:text-gray-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            ref={inputRef}
            value={query}
            onChange={e => setQuery(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Search commands, pages, actions..."
            className="flex-1 bg-transparent text-[14px] text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 outline-none"
          />
          <kbd className="text-[11px] text-gray-400 dark:text-gray-500 bg-gray-100 dark:bg-gray-800 px-1.5 py-0.5 rounded border border-gray-200 dark:border-gray-700">ESC</kbd>
        </div>
        <div className="max-h-[320px] overflow-y-auto p-2">
          {Object.entries(grouped).map(([category, catItems]) => (
            <div key={category} className="mb-2">
              <div className="px-3 py-1.5 text-[10px] font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500">{category}</div>
              {catItems.map(item => {
                const idx = filtered.indexOf(item);
                return (
                  <button
                    key={item.id}
                    onClick={() => { if (item.href) { setOpen(false); router.push(item.href); } }}
                    className={`w-full flex items-center justify-between px-3 py-2 rounded-lg text-[13px] transition-colors ${
                      idx === selectedIndex
                        ? "bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400"
                        : "text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white"
                    }`}
                  >
                    <span>{item.name}</span>
                    {item.shortcut && (
                      <kbd className="text-[10px] text-gray-400 dark:text-gray-500 bg-gray-100 dark:bg-gray-800 px-1.5 py-0.5 rounded border border-gray-200 dark:border-gray-700">{item.shortcut}</kbd>
                    )}
                  </button>
                );
              })}
            </div>
          ))}
          {filtered.length === 0 && <div className="text-center py-8 text-gray-400 dark:text-gray-500 text-[13px]">No results found</div>}
        </div>
        <div className="flex items-center gap-4 px-4 py-2.5 border-t border-gray-200 dark:border-gray-800 text-[11px] text-gray-400 dark:text-gray-500">
          <span>↑↓ navigate</span>
          <span>↵ select</span>
          <span>esc close</span>
        </div>
      </div>
    </div>
  );
}
