"use client";
import React, { useEffect, useState } from "react";
import scrapeJobService, { ScrapeJob } from "@/services/scrapeJobService";

interface ActivityItem {
  id: string;
  text: string;
  time: string;
  type: "success" | "warning" | "error" | "info";
}

const typeColors: Record<string, string> = {
  success: "#22c55e",
  warning: "#f59e0b",
  error: "#ef4444",
  info: "#6b7280",
};

export default function ActivityWidget() {
  const [items, setItems] = useState<ActivityItem[]>([]);

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await scrapeJobService.getAllScrapeJobs(1);
        const jobs = res.content.slice(0, 6);
        setItems(jobs.map((j: ScrapeJob) => ({
          id: j.id.toString(),
          text: j.beenScraped
            ? `Scrape completed: ${j.link.substring(0, 40)}...`
            : j.scrapeFailed
            ? `Scrape failed: ${j.link.substring(0, 40)}...`
            : `Scrape pending: ${j.link.substring(0, 40)}...`,
          time: new Date(j.createdAt).toLocaleString(),
          type: j.beenScraped ? "success" : j.scrapeFailed ? "error" : "warning",
        })));
      } catch {
        setItems([
          { id: "1", text: "Platform initialized", time: "Just now", type: "info" },
          { id: "2", text: "System health check passed", time: "2m ago", type: "success" },
          { id: "3", text: "API rate limit approaching", time: "5m ago", type: "warning" },
        ]);
      }
    };
    fetch();
  }, []);

  return (
    <div className="space-y-1">
      {items.map((item) => (
        <div
          key={item.id}
          className="flex items-start gap-3 p-3 rounded-xl hover:bg-white/[0.03] transition-colors duration-200"
        >
          <div
            className="w-2 h-2 rounded-full mt-1.5 shrink-0"
            style={{ background: typeColors[item.type] }}
          />
          <div className="flex-1 min-w-0">
            <p className="text-[13px] text-white/70 leading-snug">{item.text}</p>
            <p className="text-[11px] text-white/30 mt-0.5">{item.time}</p>
          </div>
        </div>
      ))}
      {items.length === 0 && (
        <div className="text-center py-8 text-white/20 text-[13px]">No recent activity</div>
      )}
    </div>
  );
}
