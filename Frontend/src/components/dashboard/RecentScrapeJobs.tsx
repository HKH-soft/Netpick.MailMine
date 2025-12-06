"use client";
import React, { useEffect, useState } from "react";
import {
    Table,
    TableBody,
    TableCell,
    TableHeader,
    TableRow,
} from "../ui/table";
import Badge from "../ui/badge/Badge";
import scrapeJobService, { ScrapeJob } from "@/services/scrapeJobService";
import Link from "next/link";

export default function RecentScrapeJobs() {
    const [jobs, setJobs] = useState<ScrapeJob[]>([]);

    useEffect(() => {
        const fetchJobs = async () => {
            try {
                const response = await scrapeJobService.getAllScrapeJobs(1);
                setJobs(response.context.slice(0, 5)); // Get top 5
            } catch (error) {
                console.error("Failed to fetch recent jobs", error);
            }
        };
        fetchJobs();
    }, []);

    return (
        <div className="h-full overflow-hidden rounded-2xl border border-gray-200 bg-white px-4 pb-3 pt-4 dark:border-gray-800 dark:bg-white/[0.03] sm:px-6">
            <div className="flex flex-col gap-2 mb-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
                        Recent Scrape Jobs
                    </h3>
                </div>
                <Link
                    href="/scrape/jobs"
                    className="inline-flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2.5 text-theme-sm font-medium text-gray-700 shadow-theme-xs hover:bg-gray-50 hover:text-gray-800 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03] dark:hover:text-gray-200"
                >
                    See all
                </Link>
            </div>
            <div className="max-w-full overflow-x-auto">
                <Table>
                    <TableHeader className="border-gray-100 dark:border-gray-800 border-y">
                        <TableRow>
                            <TableCell isHeader className="py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400">
                                Link
                            </TableCell>
                            <TableCell isHeader className="py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400">
                                Status
                            </TableCell>
                            <TableCell isHeader className="py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400">
                                Attempt
                            </TableCell>
                            <TableCell isHeader className="py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400">
                                Created At
                            </TableCell>
                        </TableRow>
                    </TableHeader>
                    <TableBody className="divide-y divide-gray-100 dark:divide-gray-800">
                        {jobs.map((job) => (
                            <TableRow key={job.id}>
                                <TableCell className="py-3 text-gray-500 text-theme-sm dark:text-gray-400 max-w-[200px] truncate">
                                    <a href={job.link} target="_blank" rel="noopener noreferrer" className="hover:text-brand-500">
                                        {job.link}
                                    </a>
                                </TableCell>
                                <TableCell className="py-3 text-gray-500 text-theme-sm dark:text-gray-400">
                                    <Badge
                                        size="sm"
                                        color={
                                            job.beenScraped
                                                ? "success"
                                                : job.scrapeFailed
                                                    ? "error"
                                                    : "warning"
                                        }
                                    >
                                        {job.beenScraped
                                            ? "Completed"
                                            : job.scrapeFailed
                                                ? "Failed"
                                                : "Pending"}
                                    </Badge>
                                </TableCell>
                                <TableCell className="py-3 text-gray-500 text-theme-sm dark:text-gray-400">
                                    {job.attempt}
                                </TableCell>
                                <TableCell className="py-3 text-gray-500 text-theme-sm dark:text-gray-400">
                                    {new Date(job.createdAt).toLocaleDateString()}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>
        </div>
    );
}
