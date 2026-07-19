"use client";
import React, { useState } from "react";
import { useDashboard } from "@/context/DashboardContext";

const widgetCatalog = [
  { type: "metrics", name: "KPI Cards", description: "Key performance indicators at a glance", category: "Analytics", size: "Full width" },
  { type: "chart", name: "Revenue Chart", description: "Interactive analytics chart with date ranges", category: "Analytics", size: "Large" },
  { type: "activity", name: "Activity Feed", description: "Recent platform activity and events", category: "Activity", size: "Medium" },
  { type: "quick-actions", name: "Quick Actions", description: "Frequently used shortcuts and actions", category: "Tools", size: "Medium" },
  { type: "recent-contacts", name: "Recent Contacts", description: "Newly discovered email contacts", category: "Data", size: "Medium" },
  { type: "recent-jobs", name: "Recent Jobs", description: "Latest scrape job activity and status", category: "Data", size: "Medium" },
];

const categories = ["All", "Analytics", "Activity", "Data", "Tools"];

export default function WidgetLibrary() {
  const { libraryOpen, setLibraryOpen, addWidget, widgets } = useDashboard();
  const [search, setSearch] = useState("");
  const [activeCategory, setActiveCategory] = useState("All");

  if (!libraryOpen) return null;

  const filtered = widgetCatalog.filter(w => {
    const matchesSearch = w.name.toLowerCase().includes(search.toLowerCase()) || w.description.toLowerCase().includes(search.toLowerCase());
    const matchesCategory = activeCategory === "All" || w.category === activeCategory;
    return matchesSearch && matchesCategory;
  });

  const isAdded = (type: string) => widgets.some(w => w.type === type);

  return (
    <div className="fixed inset-0 z-[9998] flex justify-end" onClick={() => setLibraryOpen(false)}>
      <div className="fixed inset-0 bg-black/40 dark:bg-black/40 backdrop-blur-sm" />
      <div
        className="relative w-full max-w-[420px] h-full border-l border-gray-200 dark:border-gray-800 overflow-y-auto bg-white dark:bg-gray-950"
        style={{
          boxShadow: "-20px 0 60px rgba(0,0,0,0.4)",
          animation: "slideInRight 300ms cubic-bezier(0.22, 1, 0.36, 1)",
        }}
        onClick={e => e.stopPropagation()}
      >
        <div className="sticky top-0 z-10 px-5 py-4 border-b border-gray-200 dark:border-gray-800 bg-white/95 dark:bg-gray-950/95 backdrop-blur-sm">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-[15px] font-semibold text-gray-900 dark:text-white">Widget Library</h2>
            <button onClick={() => setLibraryOpen(false)} className="p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 transition-colors">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search widgets..."
            className="w-full px-3 py-2 text-[13px] bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 outline-none focus:border-green-500/30 transition-colors"
          />
          <div className="flex gap-1.5 mt-3 overflow-x-auto no-scrollbar">
            {categories.map(cat => (
              <button
                key={cat}
                onClick={() => setActiveCategory(cat)}
                className={`px-3 py-1 text-[11px] font-medium rounded-full whitespace-nowrap transition-all ${
                  activeCategory === cat
                    ? "bg-green-50 dark:bg-green-500/15 text-green-600 dark:text-green-400 border border-green-200 dark:border-green-500/20"
                    : "text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 border border-gray-200 dark:border-gray-700"
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>

        <div className="p-4 space-y-2">
          {filtered.map(w => {
            const added = isAdded(w.type);
            return (
              <div
                key={w.type}
                className="p-4 rounded-xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 hover:bg-gray-50 dark:hover:bg-gray-800 hover:border-gray-300 dark:hover:border-gray-700 transition-all duration-200"
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <h3 className="text-[13px] font-semibold text-gray-900 dark:text-white mb-0.5">{w.name}</h3>
                    <p className="text-[12px] text-gray-500 dark:text-gray-400 leading-relaxed">{w.description}</p>
                    <div className="flex items-center gap-2 mt-2">
                      <span className="text-[10px] text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-gray-800 px-2 py-0.5 rounded-full">{w.category}</span>
                      <span className="text-[10px] text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-gray-800 px-2 py-0.5 rounded-full">{w.size}</span>
                    </div>
                  </div>
                  <button
                    onClick={() => !added && addWidget(w.type)}
                    disabled={added}
                    className={`px-3 py-1.5 text-[11px] font-medium rounded-lg transition-all ${
                      added
                        ? "bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-500 cursor-default"
                        : "bg-green-50 dark:bg-green-500/15 text-green-600 dark:text-green-400 hover:bg-green-100 dark:hover:bg-green-500/25 border border-green-200 dark:border-green-500/20"
                    }`}
                  >
                    {added ? "Added" : "Add"}
                  </button>
                </div>
              </div>
            );
          })}
          {filtered.length === 0 && (
            <div className="text-center py-12 text-gray-400 dark:text-gray-500 text-[13px]">No widgets found</div>
          )}
        </div>
      </div>
      <style jsx>{`
        @keyframes slideInRight {
          from { transform: translateX(100%); opacity: 0; }
          to { transform: translateX(0); opacity: 1; }
        }
      `}</style>
    </div>
  );
}
