"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState } from "react";
import { usePipelines } from "@/hooks/usePipelines";
import ConfirmModal from "@/components/forms/ConfirmModal";
import PipelineService from "@/services/pipelineService";
import { Pipeline } from "@/services/pipelineService";
import { useToast } from "@/context/ToastContext";

export default function Pipelines() {
    const [currentPage, setCurrentPage] = useState<number>(1);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState<Pipeline | null>(null);
    const { addToast } = useToast();
    const { pipelines, loading, error, totalPages, refetch } = usePipelines(currentPage);

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
    };

    const handleDelete = (row: Record<string, unknown>) => {
        const pipeline = pipelines.find(p => p.id === row.fullId);
        if (pipeline) {
            setSelectedItem(pipeline);
            setIsDeleteModalOpen(true);
        }
    };

    const handleDeleteConfirm = async () => {
        if (!selectedItem) return;
        try {
            await PipelineService.deletePipeline(selectedItem.id);
            addToast("success", "Success", "Pipeline deleted successfully");
            setIsDeleteModalOpen(false);
            await refetch();
        } catch (err) {
            console.error("Failed to delete pipeline:", err);
            addToast("error", "Error", "Failed to delete pipeline");
        }
    };

    const columns: ColumnConfig[] = [
        { key: "id", header: "ID", type: "text" },
        { key: "stage", header: "Stage", type: "text" },
        { key: "state", header: "Status", type: "text" },
        { key: "currentStepName", header: "Current Step", type: "text" },
        { key: "progress", header: "Progress", type: "text" },
        { key: "stats", header: "Stats", type: "text" },
        { key: "startTime", header: "Started At", type: "text" },
        { key: "delete", header: "Delete", type: "delete" },
    ];

    const data = pipelines.map(pipeline => ({
        id: pipeline.id.substring(0, 8) + "...",
        fullId: pipeline.id,
        stage: pipeline.stage || "-",
        state: pipeline.state || "-",
        currentStepName: pipeline.currentStepName || "-",
        progress: pipeline.itemsTotal > 0
            ? `${pipeline.itemsProcessed}/${pipeline.itemsTotal} (${Math.round((pipeline.itemsProcessed / pipeline.itemsTotal) * 100)}%)`
            : "-",
        stats: `Links: ${pipeline.linksCreated || 0}, Pages: ${pipeline.pagesScraped || 0}, Contacts: ${pipeline.contactsFound || 0}`,
        startTime: pipeline.startTime ? new Date(pipeline.startTime).toLocaleString() : "-",
    }));

    if (loading) {
        return <div className="flex items-center justify-center p-8">
            <div className="text-gray-500 dark:text-gray-400">Loading pipelines...</div>
        </div>;
    }

    if (error) {
        return <div className="flex items-center justify-center p-8">
            <div className="text-red-500 dark:text-red-400">Error: {error}</div>
        </div>;
    }

    return (
        <div>
            <PageBreadcrumb pageTitle="Pipeline History" />
            <div className="space-y-6">
                {pipelines.length === 0 ? (
                    <p className="text-gray-500 dark:text-gray-400 text-center py-4">No pipelines found. Start a scrape to create pipeline records.</p>
                ) : (
                    <>
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
                    </>
                )}
            </div>

            <ConfirmModal
                isOpen={isDeleteModalOpen}
                onCloseAction={() => setIsDeleteModalOpen(false)}
                onConfirm={handleDeleteConfirm}
                title="Delete Pipeline"
                message={`Are you sure you want to delete this pipeline?`}
            />
        </div>
    );
}
