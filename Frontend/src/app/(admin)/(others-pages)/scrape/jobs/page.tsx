"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState } from "react";
import { useScrapeJobs } from "@/hooks/useScrapeJobs";
import ConfirmModal from "@/components/forms/ConfirmModal";
import ScrapeJobService from "@/services/scrapeJobService";
import { ScrapeJob } from "@/services/scrapeJobService";
import { useToast } from "@/context/ToastContext";

export default function ScrapeJobs() {
    const [currentPage, setCurrentPage] = useState<number>(1);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState<ScrapeJob | null>(null);
    const { addToast } = useToast();
    const { scrapeJobs, loading, error, totalPages, refetch } = useScrapeJobs(currentPage);

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
    };

    const handleDelete = (row: Record<string, unknown>) => {
        const job = scrapeJobs.find(j => j.id === row.id);
        if (job) {
            setSelectedItem(job);
            setIsDeleteModalOpen(true);
        }
    };

    const handleDeleteConfirm = async () => {
        if (!selectedItem) return;
        try {
            await ScrapeJobService.deleteScrapeJob(selectedItem.id);
            addToast("success", "Success", "Scrape job deleted successfully");
            setIsDeleteModalOpen(false);
            await refetch();
        } catch (err) {
            console.error("Failed to delete scrape job:", err);
            addToast("error", "Error", "Failed to delete scrape job");
        }
    };

    const columns: ColumnConfig[] = [
        { key: "link", header: "Link", type: "text" },
        { key: "attempt", header: "Attempts", type: "text" },
        { key: "beenScraped", header: "Scraped", type: "text" },
        { key: "scrapeFailed", header: "Failed", type: "text" },
        { key: "createdAt", header: "Created At", type: "text" },
        { key: "delete", header: "Delete", type: "delete" },
    ];

    const data = scrapeJobs.map(job => ({
        id: job.id,
        link: job.link?.length > 50 ? job.link.substring(0, 50) + "..." : job.link || "-",
        attempt: job.attempt?.toString() || "0",
        beenScraped: job.beenScraped ? "Yes" : "No",
        scrapeFailed: job.scrapeFailed ? "Yes" : "No",
        createdAt: job.createdAt ? new Date(job.createdAt).toLocaleString() : "-",
    }));

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <>
            <PageBreadcrumb pageTitle="Scrape Jobs" />
            <DynamicTable
                columns={columns}
                data={data}
                onDelete={handleDelete}
            />
            <div className="mt-5">
                <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                />
            </div>

            <ConfirmModal
                isOpen={isDeleteModalOpen}
                onCloseAction={() => setIsDeleteModalOpen(false)}
                onConfirm={handleDeleteConfirm}
                title="Delete Scrape Job"
                message={`Are you sure you want to delete this scrape job?`}
            />
        </>
    );
}
