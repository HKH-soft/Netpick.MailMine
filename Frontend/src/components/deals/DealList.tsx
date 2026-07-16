// DealList.tsx
"use client";
import React, { useState } from "react";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import Badge from "@/components/ui/badge/Badge";
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import ConfirmModal from "@/components/forms/ConfirmModal";
import { useDeals } from "@/hooks/useDeals";
import DealService, { Deal, DealStage } from "@/services/dealService";
import { useToast } from "@/context/ToastContext";

const DEAL_STAGES: { value: DealStage; label: string }[] = [
  { value: "PROSPECTING", label: "Prospecting" },
  { value: "QUALIFICATION", label: "Qualification" },
  { value: "PROPOSAL", label: "Proposal" },
  { value: "NEGOTIATION", label: "Negotiation" },
  { value: "CLOSED_WON", label: "Closed Won" },
  { value: "CLOSED_LOST", label: "Closed Lost" },
];

const createFields = [
  { name: "title", label: "Title", type: "text", required: true },
  { name: "description", label: "Description", type: "textarea" },
  { name: "stage", label: "Stage", type: "select", required: true, options: DEAL_STAGES },
  { name: "value", label: "Value", type: "number" },
  { name: "currency", label: "Currency", type: "text", placeholder: "USD" },
  { name: "expectedCloseDate", label: "Expected Close Date", type: "date" },
  { name: "probability", label: "Probability (%)", type: "number" },
];

const editFields = [
  { name: "title", label: "Title", type: "text", required: true },
  { name: "description", label: "Description", type: "textarea" },
  { name: "stage", label: "Stage", type: "select", required: true, options: DEAL_STAGES },
  { name: "value", label: "Value", type: "number" },
  { name: "currency", label: "Currency", type: "text" },
  { name: "expectedCloseDate", label: "Expected Close Date", type: "date" },
  { name: "probability", label: "Probability (%)", type: "number" },
];

export default function DealList() {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedDeal, setSelectedDeal] = useState<Deal | null>(null);
  const { addToast } = useToast();
  const { deals, loading, error, totalPages, refetch } = useDeals(currentPage);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleCreate = () => {
    setSelectedDeal(null);
    setIsCreateModalOpen(true);
  };

  const handleEdit = (row: Record<string, unknown>) => {
    const deal = deals.find(d => d.id === row.id);
    if (deal) {
      setSelectedDeal(deal);
      setIsEditModalOpen(true);
    }
  };

  const handleDelete = (row: Record<string, unknown>) => {
    const deal = deals.find(d => d.id === row.id);
    if (deal) {
      setSelectedDeal(deal);
      setIsDeleteModalOpen(true);
    }
  };

  const handleCreateSubmit = async (data: Record<string, unknown>) => {
    try {
      await DealService.createDeal({
        title: data.title as string,
        description: data.description as string,
        stage: data.stage as string,
        value: data.value ? Number(data.value) : undefined,
        currency: (data.currency as string) || "USD",
        expectedCloseDate: data.expectedCloseDate as string,
        probability: data.probability ? Number(data.probability) : undefined,
      });
      addToast("success", "Success", "Deal created successfully");
      setIsCreateModalOpen(false);
      await refetch();
    } catch (err) {
      console.error("Failed to create deal:", err);
      addToast("error", "Error", "Failed to create deal");
    }
  };

  const handleEditSubmit = async (data: Record<string, unknown>) => {
    if (!selectedDeal) return;
    try {
      await DealService.updateDeal(selectedDeal.id, {
        title: data.title as string,
        description: data.description as string,
        stage: data.stage as string,
        value: data.value ? Number(data.value) : undefined,
        currency: data.currency as string,
        expectedCloseDate: data.expectedCloseDate as string,
        probability: data.probability ? Number(data.probability) : undefined,
      });
      addToast("success", "Success", "Deal updated successfully");
      setIsEditModalOpen(false);
      await refetch();
    } catch (err) {
      console.error("Failed to update deal:", err);
      addToast("error", "Error", "Failed to update deal");
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedDeal) return;
    try {
      await DealService.deleteDeal(selectedDeal.id);
      addToast("success", "Success", "Deal deleted successfully");
      setIsDeleteModalOpen(false);
      await refetch();
    } catch (err) {
      console.error("Failed to delete deal:", err);
      addToast("error", "Error", "Failed to delete deal");
    }
  };

  const getStageColor = (stage: string) => {
    switch (stage) {
      case "PROSPECTING":
        return "info";
      case "QUALIFICATION":
        return "warning";
      case "PROPOSAL":
        return "primary";
      case "NEGOTIATION":
        return "dark";
      case "CLOSED_WON":
        return "success";
      case "CLOSED_LOST":
        return "error";
      default:
        return "light";
    }
  };

  const columns: ColumnConfig[] = [
    { key: "title", header: "Title", type: "text" },
    { 
      key: "stage", 
      header: "Stage", 
      type: "status",
      render: (value) => (
        <Badge size="sm" color={getStageColor(String(value))}>
          {String(value).replace("_", " ")}
        </Badge>
      )
    },
    { 
      key: "value", 
      header: "Value", 
      type: "text",
      render: (value) => `$${value || 0}`
    },
    { key: "currency", header: "Currency", type: "text" },
    { key: "createdAt", header: "Created At", type: "text" },
    { key: "edit", header: "Edit", type: "edit" },
    { key: "delete", header: "Delete", type: "delete" },
  ];

  const data = deals.map(deal => ({
    id: deal.id,
    title: deal.title,
    stage: deal.stage,
    value: deal.value,
    currency: deal.currency,
    createdAt: deal.createdAt ? new Date(deal.createdAt).toLocaleDateString() : "-",
  }));

  if (loading) {
    return <div className="p-4">Loading...</div>;
  }

  if (error) {
    return <div className="p-4 text-red-500">Error: {error}</div>;
  }

  return (
    <div className="space-y-4">
      <div>
        <Button onClick={handleCreate}>New Deal</Button>
      </div>
      <DynamicTable
        columns={columns}
        data={data}
        onEdit={handleEdit}
        onDelete={handleDelete}
      />
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={handlePageChange}
      />
      <ModalForm
        isOpen={isCreateModalOpen}
        onCloseAction={() => setIsCreateModalOpen(false)}
        onSubmit={handleCreateSubmit}
        title="Create Deal"
        fields={createFields}
        submitButtonText="Create"
      />
      <ModalForm
        isOpen={isEditModalOpen}
        onCloseAction={() => setIsEditModalOpen(false)}
        onSubmit={handleEditSubmit}
        title="Edit Deal"
        fields={editFields}
        initialData={selectedDeal ? {
          title: selectedDeal.title,
          description: selectedDeal.description || "",
          stage: selectedDeal.stage,
          value: selectedDeal.value?.toString() || "",
          currency: selectedDeal.currency,
          expectedCloseDate: selectedDeal.expectedCloseDate?.split("T")[0] || "",
          probability: selectedDeal.probability?.toString() || "",
        } : undefined}
        submitButtonText="Update"
      />
      <ConfirmModal
        isOpen={isDeleteModalOpen}
        onCloseAction={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteConfirm}
        title="Delete Deal"
        message={`Are you sure you want to delete "${selectedDeal?.title}"?`}
      />
    </div>
  );
}