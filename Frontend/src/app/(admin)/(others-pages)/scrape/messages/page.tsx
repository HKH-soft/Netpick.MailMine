"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState } from "react";
import { useScrapeJobs } from "@/hooks/useScrapeJobs";
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import ConfirmModal from "@/components/forms/ConfirmModal";
import ScrapeJobService from "@/services/scrapeJobService";
import { ScrapeJob } from "@/services/scrapeJobService";
import { useToast } from "@/context/ToastContext";

// Define types for our data

export default function Messages() {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<ScrapeJob | null>(null);
  const { addToast } = useToast();
  const { scrapeJobs, loading, error, totalPages, refetch } = useScrapeJobs(currentPage);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleCreate = () => {
    setSelectedItem(null);
    setIsCreateModalOpen(true);
  };

  const handleEdit = (row: Record<string, unknown>) => {
    setSelectedItem({
      id: row.id as string,
      link: row.link as string,
      attempt: row.attempt as number,
      beenScraped: row.beenScraped as boolean,
      scrapeFailed: row.scrapeFailed as boolean,
      description: row.description as string,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
    setIsEditModalOpen(true);
  };

  const handleDelete = (row: Record<string, unknown>) => {
    setSelectedItem({
      id: row.id as string,
      link: row.link as string,
      attempt: row.attempt as number,
      beenScraped: row.beenScraped as boolean,
      scrapeFailed: row.scrapeFailed as boolean,
      description: row.description as string,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
    setIsDeleteModalOpen(true);
  };

  const handleCreateSubmit = async (data: Record<string, unknown>) => {
    try {
      await ScrapeJobService.createScrapeJob(data.link as string, data.description as string);
      addToast("success", "Success", "Scrape job created successfully");
      // Close the modal
      setIsCreateModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to create scrape job:", err);
      addToast("error", "Error", "Failed to create scrape job");
    }
  };

  const handleEditSubmit = async (data: Record<string, unknown>) => {
    if (!selectedItem) return;
    try {
      await ScrapeJobService.updateScrapeJob(selectedItem.id, {
        link: data.link as string,
        description: data.description as string
      });
      addToast("success", "Success", "Scrape job updated successfully");
      // Close the modal
      setIsEditModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to update scrape job:", err);
      addToast("error", "Error", "Failed to update scrape job");
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedItem) return;
    try {
      await ScrapeJobService.deleteScrapeJob(selectedItem.id);
      addToast("success", "Success", "Scrape job deleted successfully");
      // Close the modal
      setIsDeleteModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to delete scrape job:", err);
      addToast("error", "Error", "Failed to delete scrape job");
    }
  };

  const columns = [
    { key: "link", header: "Link", type: "text" },
    { key: "attempt", header: "Attempts", type: "text" },
    { key: "status", header: "Status", type: "status" },
    { key: "edit", header: "Edit", type: "edit" },
    { key: "delete", header: "Delete", type: "delete" },
  ] as const satisfies ColumnConfig[];

  // Transform data to match the table structure
  const data = scrapeJobs.map(job => ({
    id: job.id,
    link: job.link,
    attempt: job.attempt.toString(),
    status: job.beenScraped ? "Completed" : job.scrapeFailed ? "Failed" : "Pending",
  }));

  const createFields = [
    { name: "link", label: "Link", type: "text", required: true },
    { name: "description", label: "Description", type: "textarea" },
  ];

  const editFields = [
    { name: "link", label: "Link", type: "text", required: true },
    { name: "description", label: "Description", type: "textarea" },
  ];

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <>
      <PageBreadcrumb pageTitle="Messages" />
      <div className="mb-4">
        <Button onClick={handleCreate}>Create Scrape Job</Button>
      </div>
      <DynamicTable columns={columns} data={data} onEdit={handleEdit} onDelete={handleDelete} />
      <div className="mt-5">
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={handlePageChange}
        />
      </div>

      <ModalForm
        isOpen={isCreateModalOpen}
        onCloseAction={() => setIsCreateModalOpen(false)}
        onSubmit={handleCreateSubmit}
        title="Create Scrape Job"
        fields={createFields}
        submitButtonText="Create"
      />

      <ModalForm
        isOpen={isEditModalOpen}
        onCloseAction={() => setIsEditModalOpen(false)}
        onSubmit={handleEditSubmit}
        title="Edit Scrape Job"
        fields={editFields}
        initialData={selectedItem ? (selectedItem as unknown as Record<string, unknown>) : undefined}
        submitButtonText="Update"
      />

      <ConfirmModal
        isOpen={isDeleteModalOpen}
        onCloseAction={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteConfirm}
        title="Delete Scrape Job"
        message={`Are you sure you want to delete the scrape job for "${selectedItem?.link}"?`}
      />
    </>
  );
}