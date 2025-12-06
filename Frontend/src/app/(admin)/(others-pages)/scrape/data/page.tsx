"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState } from "react";
import { useScrapeDataList } from "@/hooks/useScrapeData";
import ConfirmModal from "@/components/forms/ConfirmModal";
import ScrapeDataService from "@/services/scrapeDataService";
import { ScrapeData } from "@/services/scrapeDataService";
import { useToast } from "@/context/ToastContext";

export default function ScrapeDataPage() {
    const [currentPage, setCurrentPage] = useState<number>(1);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState<ScrapeData | null>(null);
    const { addToast } = useToast();
    const { scrapeDataList, loading, error, totalPages, refetch } = useScrapeDataList(currentPage);

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
    };

    const handleDelete = (row: Record<string, unknown>) => {
        const data = scrapeDataList.find(d => d.id === row.id);
        if (data) {
            setSelectedItem(data);
            setIsDeleteModalOpen(true);
        }
    };

    const handleDeleteConfirm = async () => {
        if (!selectedItem) return;
        try {
            await ScrapeDataService.deleteScrapeData(selectedItem.id);
            addToast("success", "Success", "Scrape data deleted successfully");
            setIsDeleteModalOpen(false);
            await refetch();
        } catch (err) {
            console.error("Failed to delete scrape data:", err);
            addToast("error", "Error", "Failed to delete scrape data");
        }
    };

    const columns: ColumnConfig[] = [
        { key: "fileName", header: "File Name", type: "text" },
        { key: "attemptNumber", header: "Attempt #", type: "text" },
        { key: "parsed", header: "Parsed", type: "text" },
        { key: "createdAt", header: "Created At", type: "text" },
        { key: "delete", header: "Delete", type: "delete" },
    ];

    const data = scrapeDataList.map(item => ({
        id: item.id,
        fileName: item.fileName || "-",
        attemptNumber: item.attemptNumber?.toString() || "0",
        parsed: item.parsed ? "Yes" : "No",
        createdAt: item.createdAt ? new Date(item.createdAt).toLocaleString() : "-",
    }));

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <>
            <PageBreadcrumb pageTitle="Scrape Data" />
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
                title="Delete Scrape Data"
                message={`Are you sure you want to delete this scrape data?`}
            />
        </>
    );
}
