"use client";
import React, { useEffect, useState, useRef } from "react";
import { useTranslation } from "react-i18next";
import { GroupIcon, BoxIconLine, GridIcon, PlugInIcon } from "@/icons";
import contactService, { ContactStats } from "@/services/contactService";
import scrapeJobService, { ScrapeJobStats } from "@/services/scrapeJobService";
import pipelineService, { PipelineStats } from "@/services/pipelineService";
import proxyService, { ProxyStats } from "@/services/proxyService";

function AnimatedCounter({ value }: { value: number }) {
  const [display, setDisplay] = useState(0);
  const ref = useRef<number | null>(null);

  useEffect(() => {
    const start = performance.now();
    const duration = 800;
    const animate = (now: number) => {
      const elapsed = now - start;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      setDisplay(Math.round(eased * value));
      if (progress < 1) ref.current = requestAnimationFrame(animate);
    };
    ref.current = requestAnimationFrame(animate);
    return () => { if (ref.current) cancelAnimationFrame(ref.current); };
  }, [value]);

  return <span>{display.toLocaleString()}</span>;
}

export const DashboardMetrics = () => {
  const { t } = useTranslation('common');
  const [contactStats, setContactStats] = useState<ContactStats | null>(null);
  const [jobStats, setJobStats] = useState<ScrapeJobStats | null>(null);
  const [pipelineStats, setPipelineStats] = useState<PipelineStats | null>(null);
  const [proxyStats, setProxyStats] = useState<ProxyStats | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [contacts, jobs, pipelines, proxies] = await Promise.all([
          contactService.getStats(),
          scrapeJobService.getStats(),
          pipelineService.getStats(),
          proxyService.getStats(),
        ]);
        setContactStats(contacts);
        setJobStats(jobs);
        setPipelineStats(pipelines);
        setProxyStats(proxies);
      } catch (error) {
        console.error("Failed to fetch dashboard stats", error);
      }
    };
    fetchData();
  }, []);

  const metrics = [
    { icon: GroupIcon, label: t('dashboard.totalContacts'), value: contactStats?.total || 0, color: "text-green-400" },
    { icon: BoxIconLine, label: t('dashboard.remainingJobs'), value: jobStats?.pending || 0, subtext: `${t('dashboard.total')}: ${jobStats?.total || 0}`, color: "text-green-400" },
    { icon: GridIcon, label: t('dashboard.activePipelines'), value: pipelineStats?.active || 0, subtext: `${t('dashboard.total')}: ${pipelineStats?.total || 0}`, color: "text-green-400" },
    { icon: PlugInIcon, label: t('dashboard.activeProxies'), value: proxyStats?.active || 0, subtext: `${t('dashboard.total')}: ${proxyStats?.total || 0}`, color: "text-green-400" },
  ];

  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
      {metrics.map((metric, index) => (
        <div
          key={index}
          className="p-4 rounded-xl border border-gray-200/[0.1] bg-gray-50 dark:border-gray-800/[0.1] dark:bg-gray-900 hover:bg-gray-100 dark:hover:bg-gray-800 hover:border-gray-200/[0.2] dark:hover:border-gray-800/[0.2] transition-all duration-300"
          style={{ animationDelay: `${index * 80}ms`, animation: "fadeInUp 0.5s ease-out forwards", opacity: 0 }}
        >
          <div className="flex items-center justify-center w-9 h-9 p-1 rounded-xl bg-green-500/[0.08] mb-3">
            <metric.icon className={`size-5 ${metric.color}`} />
          </div>
          <div>
            <span className="text-[11px] text-gray-500 dark:text-gray-400 block font-medium">{metric.label}</span>
            <span className="font-bold text-gray-900 dark:text-white text-lg block leading-tight mt-0.5">
              <AnimatedCounter value={metric.value} />
            </span>
            {metric.subtext && (
              <span className="text-[11px] text-gray-400 dark:text-gray-500 block mt-0.5">{metric.subtext}</span>
            )}
          </div>
        </div>
      ))}
      <style jsx>{`
        @keyframes fadeInUp {
          from { opacity: 0; transform: translateY(12px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  );
};
