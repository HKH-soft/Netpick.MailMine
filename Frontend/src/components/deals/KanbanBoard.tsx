// KanbanBoard.tsx
"use client";
import React from "react";
import Badge from "@/components/ui/badge/Badge";
import { useDeals } from "@/hooks/useDeals";
import { DealStage } from "@/services/dealService";

const STAGES: DealStage[] = [
  "PROSPECTING",
  "QUALIFICATION", 
  "PROPOSAL",
  "NEGOTIATION",
  "CLOSED_WON",
  "CLOSED_LOST"
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
  const { deals, loading, error } = useDeals(1);

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
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 overflow-x-auto">
      {STAGES.map(stage => (
        <div key={stage} className="bg-gray-50 dark:bg-white/[0.03] rounded-lg p-4">
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
                className="bg-white dark:bg-white/[0.05] rounded-lg p-3 shadow-sm border border-gray-200 dark:border-white/[0.08] cursor-pointer hover:shadow-md transition-shadow"
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
  );
}