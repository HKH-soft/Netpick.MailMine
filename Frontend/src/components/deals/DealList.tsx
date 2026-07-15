// DealList.tsx
"use client";
import React, { useState } from "react";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import Badge from "@/components/ui/badge/Badge";
import { useDeals } from "@/hooks/useDeals";
import DealService from "@/services/dealService";
import { Deal } from "@/services/dealService";
import { useToast } from "@/context/ToastContext";
import ConfirmModal from "@/components/forms/ConfirmModal";

export default function DealList() {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedDeal, setSelectedDeal] = useState<Deal | null>(null);
  const { addToast } = useToast();
  const { deals, loading, error, totalPages, refetch } = useDeals(currentPage);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleDelete = (row: Record<string, unknown>) => {
    const deal = deals.find(d => d.id === row.id);
    if (deal) {
      setSelectedDeal(deal);
      setIsDeleteModalOpen(true);
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
      <DynamicTable
        columns={columns}
        data={data}
        onDelete={handleDelete}
      />
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={handlePageChange}
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