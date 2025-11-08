"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState } from "react";
import { useApiKeys } from "@/hooks/useApiKeys";
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import ConfirmModal from "@/components/forms/ConfirmModal";
import ApiKeyService from "@/services/apiKeyService";
import { ApiKey } from "@/services/apiKeyService";
import { useToast } from "@/context/ToastContext";

// Define types for our data

export default function Apikeys() {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<ApiKey | null>(null);
  const { addToast } = useToast();
  const { apiKeys, loading, error, totalPages, refetch } = useApiKeys(currentPage);

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
      key: row.key as string,
      point: row.point as number,
      searchEngineId: row.searchEngineId as string,
      apiLink: row.apiLink as string,
      description: row.description as string,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
    setIsEditModalOpen(true);
  };

  const handleDelete = (row: Record<string, unknown>) => {
    setSelectedItem({
      id: row.id as string,
      key: row.key as string,
      point: row.point as number,
      searchEngineId: row.searchEngineId as string,
      apiLink: row.apiLink as string,
      description: row.description as string,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
    setIsDeleteModalOpen(true);
  };

  const handleCreateSubmit = async (data: Record<string, unknown>) => {
    try {
      await ApiKeyService.createApiKey({
        key: data.key as string,
        point: parseInt(data.point as string) || 0,
        searchEngineId: data.searchEngineId as string,
        apiLink: data.apiLink as string,
        description: data.description as string
      });
      addToast("success", "Success", "API key created successfully");
      // Close the modal
      setIsCreateModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to create API key:", err);
      addToast("error", "Error", "Failed to create API key");
    }
  };

  const handleEditSubmit = async (data: Record<string, unknown>) => {
    if (!selectedItem) return;
    try {
      await ApiKeyService.updateApiKey(selectedItem.id, {
        key: data.key as string,
        point: parseInt(data.point as string) || 0,
        searchEngineId: data.searchEngineId as string,
        apiLink: data.apiLink as string,
        description: data.description as string
      });
      addToast("success", "Success", "API key updated successfully");
      // Close the modal
      setIsEditModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to update API key:", err);
      addToast("error", "Error", "Failed to update API key");
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedItem) return;
    try {
      await ApiKeyService.deleteApiKey(selectedItem.id);
      addToast("success", "Success", "API key deleted successfully");
      // Close the modal
      setIsDeleteModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to delete API key:", err);
      addToast("error", "Error", "Failed to delete API key");
    }
  };

  const columns = [
    { key: "key", header: "Key", type: "text" },
    { key: "description", header: "Description", type: "text" },
    { key: "point", header: "Points", type: "text" },
    { key: "edit", header: "Edit", type: "edit" },
    { key: "delete", header: "Delete", type: "delete" },
  ] as const satisfies ColumnConfig[];

  // Transform data to match the table structure
  const data = apiKeys.map(apiKey => ({
    id: apiKey.id,
    key: apiKey.key,
    description: apiKey.description || "-",
    point: apiKey.point?.toString() || "0",
  }));

  const createFields = [
    { name: "key", label: "Key", type: "text", required: true },
    { name: "point", label: "Points", type: "number" },
    { name: "searchEngineId", label: "Search Engine ID", type: "text" },
    { name: "apiLink", label: "API Link", type: "text" },
    { name: "description", label: "Description", type: "textarea" },
  ];

  const editFields = [
    { name: "key", label: "Key", type: "text", required: true },
    { name: "point", label: "Points", type: "number" },
    { name: "searchEngineId", label: "Search Engine ID", type: "text" },
    { name: "apiLink", label: "API Link", type: "text" },
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
      <PageBreadcrumb pageTitle="API Keys" />
      <div className="mb-4">
        <Button onClick={handleCreate}>Create API Key</Button>
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
        title="Create API Key"
        fields={createFields}
        submitButtonText="Create"
      />

      <ModalForm
        isOpen={isEditModalOpen}
        onCloseAction={() => setIsEditModalOpen(false)}
        onSubmit={handleEditSubmit}
        title="Edit API Key"
        fields={editFields}
        initialData={selectedItem ? (selectedItem as unknown as Record<string, unknown>) : undefined}
        submitButtonText="Update"
      />

      <ConfirmModal
        isOpen={isDeleteModalOpen}
        onCloseAction={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteConfirm}
        title="Delete API Key"
        message={`Are you sure you want to delete the API key "${selectedItem?.key}"?`}
      />
    </>
  );
}