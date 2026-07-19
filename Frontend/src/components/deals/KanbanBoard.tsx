// KanbanBoard.tsx
"use client";
import React, { useState } from "react";
import Badge from "@/components/ui/badge/Badge";
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import { useDeals } from "@/hooks/useDeals";
import DealService, { Deal, DealStage } from "@/services/dealService";
import { useToast } from "@/context/ToastContext";

const STAGES: DealStage[] = [
  "PROSPECTING",
  "QUALIFICATION", 
  "PROPOSAL",
  "NEGOTIATION",
  "CLOSED_WON",
  "CLOSED_LOST"
];

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

export default function KanbanBoard() {
  const { deals, loading, error, refetch } = useDeals(1);
  const { addToast } = useToast();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedDeal, setSelectedDeal] = useState<Deal | null>(null);

  const handleCardClick = (deal: Deal) => {
    setSelectedDeal(deal);
    setIsEditModalOpen(true);
  };

  const handleCreate = () => {
    setSelectedDeal(null);
    setIsCreateModalOpen(true);
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

  if (loading) {
    return <div className="p-4">Loading...</div>;
  }

  if (error) {
    return <div className="p-4 text-red-500">Error: {error}</div>;
  }

  const dealsByStage = STAGES.reduce((acc, stage) => {
    acc[stage] = deals.filter(deal => deal.stage === stage);
    return acc;
  }, {} as Record<string, typeof deals>);

  return (
    <div className="space-y-4">
      <div>
        <Button onClick={handleCreate}>New Deal</Button>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 overflow-x-auto">
        {STAGES.map(stage => (
          <div key={stage} className="bg-gray-50 dark:bg-gray-900 rounded-lg p-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold text-gray-800 dark:text-white/90">
                {stage.replace("_", " ")}
              </h3>
              <Badge size="sm" color={getStageColor(stage)}>
                {dealsByStage[stage].length}
              </Badge>
            </div>
            <div className="space-y-3">
              {dealsByStage[stage].map(deal => (
                <div
                  key={deal.id}
                  className="bg-white dark:bg-gray-900 rounded-lg p-3 shadow-sm border border-gray-200 dark:border-gray-800 cursor-pointer hover:shadow-md transition-shadow"
                  onClick={() => handleCardClick(deal)}
                >
                  <h4 className="font-medium text-gray-800 dark:text-white/90 mb-2">
                    {deal.title}
                  </h4>
                  <div className="text-sm text-gray-500 dark:text-gray-400">
                    ${deal.value || 0} {deal.currency}
                  </div>
                  {deal.expectedCloseDate && (
                    <div className="text-xs text-gray-400 mt-2">
                      Due: {new Date(deal.expectedCloseDate).toLocaleDateString()}
                    </div>
                  )}
                </div>
              ))}
              {dealsByStage[stage].length === 0 && (
                <div className="text-center text-gray-400 text-sm py-4">
                  No deals in this stage
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
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
    </div>
  );
}