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
      <div className="fixed inset-0 bg-black/40 backdrop-blur-sm" />
      <div
        className="relative w-full max-w-[420px] h-full border-l border-white/[0.06] overflow-y-auto"
        style={{
          background: "linear-gradient(180deg, rgba(16,16,16,0.98) 0%, rgba(10,10,10,0.98) 100%)",
          boxShadow: "-20px 0 60px rgba(0,0,0,0.4)",
          animation: "slideInRight 300ms cubic-bezier(0.22, 1, 0.36, 1)",
        }}
        onClick={e => e.stopPropagation()}
      >
        <div className="sticky top-0 z-10 px-5 py-4 border-b border-white/[0.06]" style={{ background: "rgba(16,16,16,0.95)", backdropFilter: "blur(12px)" }}>
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-[15px] font-semibold text-white/90">Widget Library</h2>
            <button onClick={() => setLibraryOpen(false)} className="p-1.5 rounded-lg hover:bg-white/[0.06] text-white/40 hover:text-white/70 transition-colors">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search widgets..."
            className="w-full px-3 py-2 text-[13px] bg-white/[0.04] border border-white/[0.06] rounded-xl text-white/80 placeholder:text-white/25 outline-none focus:border-green-500/30 transition-colors"
          />
          <div className="flex gap-1.5 mt-3 overflow-x-auto no-scrollbar">
            {categories.map(cat => (
              <button
                key={cat}
                onClick={() => setActiveCategory(cat)}
                className={`px-3 py-1 text-[11px] font-medium rounded-full whitespace-nowrap transition-all ${
                  activeCategory === cat
                    ? "bg-green-500/15 text-green-400 border border-green-500/20"
                    : "text-white/30 hover:text-white/50 border border-white/[0.04] hover:border-white/[0.08]"
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
                className="p-4 rounded-xl border border-white/[0.05] bg-white/[0.02] hover:bg-white/[0.04] hover:border-white/[0.08] transition-all duration-200"
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <h3 className="text-[13px] font-semibold text-white/80 mb-0.5">{w.name}</h3>
                    <p className="text-[12px] text-white/35 leading-relaxed">{w.description}</p>
                    <div className="flex items-center gap-2 mt-2">
                      <span className="text-[10px] text-white/20 bg-white/[0.04] px-2 py-0.5 rounded-full">{w.category}</span>
                      <span className="text-[10px] text-white/20 bg-white/[0.04] px-2 py-0.5 rounded-full">{w.size}</span>
                    </div>
                  </div>
                  <button
                    onClick={() => !added && addWidget(w.type)}
                    disabled={added}
                    className={`px-3 py-1.5 text-[11px] font-medium rounded-lg transition-all ${
                      added
                        ? "bg-white/[0.04] text-white/20 cursor-default"
                        : "bg-green-500/15 text-green-400 hover:bg-green-500/25 border border-green-500/20"
                    }`}
                  >
                    {added ? "Added" : "Add"}
                  </button>
                </div>
              </div>
            );
          })}
          {filtered.length === 0 && (
            <div className="text-center py-12 text-white/20 text-[13px]">No widgets found</div>
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
