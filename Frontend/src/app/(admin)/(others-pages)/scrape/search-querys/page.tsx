"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState } from "react";
import { useSearchQueries } from "@/hooks/useSearchQueries";
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import ConfirmModal from "@/components/forms/ConfirmModal";
import SearchQueryService from "@/services/searchQueryService";
import type { SearchQuery } from "@/services/searchQueryService";
import { useToast } from "@/context/ToastContext";

// Define types for our data

export default function SearchQuery() {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<SearchQuery | null>(null);
  const { addToast } = useToast();
  const { searchQueries, loading, error, totalPages, refetch } = useSearchQueries(currentPage);

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
      sentence: row.sentence as string,
      link_count: row.link_count as number,
      description: row.description as string,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
    setIsEditModalOpen(true);
  };

  const handleDelete = (row: Record<string, unknown>) => {
    setSelectedItem({
      id: row.id as string,
      sentence: row.sentence as string,
      link_count: row.link_count as number,
      description: row.description as string,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
    setIsDeleteModalOpen(true);
  };

  const handleCreateSubmit = async (data: Record<string, unknown>) => {
    try {
      await SearchQueryService.createSearchQuery({
        sentence: data.sentence as string,
        link_count: parseInt(data.link_count as string) || 0,
        description: data.description as string
      });
      addToast("success", "Success", "Search query created successfully");
      // Close the modal
      setIsCreateModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to create search query:", err);
      addToast("error", "Error", "Failed to create search query");
    }
  };

  const handleEditSubmit = async (data: Record<string, unknown>) => {
    if (!selectedItem) return;
    try {
      await SearchQueryService.updateSearchQuery(selectedItem.id, {
        sentence: data.sentence as string,
        link_count: parseInt(data.link_count as string) || 0,
        description: data.description as string
      });
      addToast("success", "Success", "Search query updated successfully");
      // Close the modal
      setIsEditModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to update search query:", err);
      addToast("error", "Error", "Failed to update search query");
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedItem) return;
    try {
      await SearchQueryService.deleteSearchQuery(selectedItem.id);
      addToast("success", "Success", "Search query deleted successfully");
      // Close the modal
      setIsDeleteModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to delete search query:", err);
      addToast("error", "Error", "Failed to delete search query");
    }
  };

  const columns = [
    { key: "sentence", header: "Query", type: "text" },
    { key: "link_count", header: "Results", type: "text" },
    { key: "description", header: "Description", type: "text" },
    { key: "edit", header: "Edit", type: "edit" },
    { key: "delete", header: "Delete", type: "delete" },
  ] as const satisfies ColumnConfig[];

  // Transform data to match the table structure
  const data = searchQueries.map(searchQuery => ({
    id: searchQuery.id,
    sentence: searchQuery.sentence,
    link_count: searchQuery.link_count.toString(),
    description: searchQuery.description || "-",
  }));

  const createFields = [
    { name: "sentence", label: "Query Sentence", type: "text", required: true },
    { name: "link_count", label: "Link Count", type: "number" },
    { name: "description", label: "Description", type: "textarea" },
  ];

  const editFields = [
    { name: "sentence", label: "Query Sentence", type: "text", required: true },
    { name: "link_count", label: "Link Count", type: "number" },
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
      <PageBreadcrumb pageTitle="Search Query" />
      <div className="mb-4">
        <Button onClick={handleCreate}>Create Search Query</Button>
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
        title="Create Search Query"
        fields={createFields}
        submitButtonText="Create"
      />

      <ModalForm
        isOpen={isEditModalOpen}
        onCloseAction={() => setIsEditModalOpen(false)}
        onSubmit={handleEditSubmit}
        title="Edit Search Query"
        fields={editFields}
        initialData={selectedItem ? (selectedItem as unknown as Record<string, unknown>) : undefined}
        submitButtonText="Update"
      />

      <ConfirmModal
        isOpen={isDeleteModalOpen}
        onCloseAction={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteConfirm}
        title="Delete Search Query"
        message={`Are you sure you want to delete the search query "${selectedItem?.sentence}"?`}
      />
    </>
  );
}