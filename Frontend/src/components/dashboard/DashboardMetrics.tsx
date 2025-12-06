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

    return (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 md:gap-6 xl:grid-cols-4">
            {/* <!-- Metric Item Start --> */}
            <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] md:p-6">
                <div className="flex items-center justify-center w-12 h-12 bg-gray-100 rounded-xl dark:bg-gray-800">
                    <GroupIcon className="text-gray-800 size-6 dark:text-white/90" />
                </div>

                <div className="flex items-end justify-between mt-5">
                    <div>
                        <span className="text-sm text-gray-500 dark:text-gray-400">
                            Total Contacts
                        </span>
                        <h4 className="mt-2 font-bold text-gray-800 text-title-sm dark:text-white/90">
                            {contactStats?.total || 0}
                        </h4>
                    </div>
                </div>
            </div>
            {/* <!-- Metric Item End --> */}

            {/* <!-- Metric Item Start --> */}
            <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] md:p-6">
                <div className="flex items-center justify-center w-12 h-12 bg-gray-100 rounded-xl dark:bg-gray-800">
                    <BoxIconLine className="text-gray-800 dark:text-white/90" />
                </div>
                <div className="flex items-end justify-between mt-5">
                    <div>
                        <span className="text-sm text-gray-500 dark:text-gray-400">
                            Remaining Jobs
                        </span>
                        <h4 className="mt-2 font-bold text-gray-800 text-title-sm dark:text-white/90">
                            {jobStats?.pending || 0}
                        </h4>
                    </div>
                    <div className="text-xs text-gray-500">
                        Total: {jobStats?.total || 0}
                    </div>
                </div>
            </div>
            {/* <!-- Metric Item End --> */}

            {/* <!-- Metric Item Start --> */}
            <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] md:p-6">
                <div className="flex items-center justify-center w-12 h-12 bg-gray-100 rounded-xl dark:bg-gray-800">
                    <GridIcon className="text-gray-800 dark:text-white/90" />
                </div>
                <div className="flex items-end justify-between mt-5">
                    <div>
                        <span className="text-sm text-gray-500 dark:text-gray-400">
                            Active Pipelines
                        </span>
                        <h4 className="mt-2 font-bold text-gray-800 text-title-sm dark:text-white/90">
                            {pipelineStats?.active || 0}
                        </h4>
                    </div>
                    <div className="text-xs text-gray-500">
                        Total: {pipelineStats?.total || 0}
                    </div>
                </div>
            </div>
            {/* <!-- Metric Item End --> */}

            {/* <!-- Metric Item Start --> */}
            <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] md:p-6">
                <div className="flex items-center justify-center w-12 h-12 bg-gray-100 rounded-xl dark:bg-gray-800">
                    <PlugInIcon className="text-gray-800 dark:text-white/90" />
                </div>
                <div className="flex items-end justify-between mt-5">
                    <div>
                        <span className="text-sm text-gray-500 dark:text-gray-400">
                            Active Proxies
                        </span>
                        <h4 className="mt-2 font-bold text-gray-800 text-title-sm dark:text-white/90">
                            {proxyStats?.active || 0}
                        </h4>
                    </div>
                    <div className="text-xs text-gray-500">
                        Total: {proxyStats?.total || 0}
                    </div>
                </div>
            </div>
            {/* <!-- Metric Item End --> */}
        </div>
    );
};
