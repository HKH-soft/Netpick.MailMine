"use client";
import React, { useEffect, useState } from "react";
import { GroupIcon, BoxIconLine, GridIcon, PlugInIcon } from "@/icons";
import contactService, { ContactStats } from "@/services/contactService";
import scrapeJobService, { ScrapeJobStats } from "@/services/scrapeJobService";
import pipelineService, { PipelineStats } from "@/services/pipelineService";
import proxyService, { ProxyStats } from "@/services/proxyService";

export const DashboardMetrics = () => {
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
        { icon: GroupIcon, label: "Total Contacts", value: contactStats?.total || 0, color: "text-gray-800" },
        { icon: BoxIconLine, label: "Remaining Jobs", value: jobStats?.pending || 0, subtext: `Total: ${jobStats?.total || 0}`, color: "text-gray-800" },
        { icon: GridIcon, label: "Active Pipelines", value: pipelineStats?.active || 0, subtext: `Total: ${pipelineStats?.total || 0}`, color: "text-gray-800" },
        { icon: PlugInIcon, label: "Active Proxies", value: proxyStats?.active || 0, subtext: `Total: ${proxyStats?.total || 0}`, color: "text-gray-800" },
    ];

    return (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {metrics.map((metric, index) => (
                <div key={index} className="rounded-2xl border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-white/[0.03]">
                    <div className="flex items-center justify-center w-10 h-10 bg-gray-100 rounded-xl dark:bg-gray-800 mb-3">
                        <metric.icon className={`size-5 ${metric.color} dark:text-white/90`} />
                    </div>
                    <div>
                        <span className="text-xs text-gray-500 dark:text-gray-400 block">
                            {metric.label}
                        </span>
                        <span className="font-bold text-gray-800 text-lg dark:text-white/90">
                            {metric.value}
                        </span>
                        {metric.subtext && (
                            <span className="text-xs text-gray-500 dark:text-gray-400 block">
                                {metric.subtext}
                            </span>
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
};



