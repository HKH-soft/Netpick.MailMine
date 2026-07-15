"use client";

import React, { useState } from "react";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import KanbanBoard from "@/components/deals/KanbanBoard";
import DealList from "@/components/deals/DealList";
import Button from "@/components/ui/button/Button";

export default function DealFarmPage() {
  const [viewMode, setViewMode] = useState<"kanban" | "list">("kanban");

  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <PageBreadcrumb pageTitle="DealPick" />
        <div className="flex items-center justify-between mt-4">
          <p className="text-gray-500 dark:text-gray-400">
            Manage deals, contacts, and sales pipeline
          </p>
          <div className="flex gap-2">
            <Button
              size="sm"
              variant={viewMode === "kanban" ? "primary" : "outline"}
              onClick={() => setViewMode("kanban")}
            >
              Kanban
            </Button>
            <Button
              size="sm"
              variant={viewMode === "list" ? "primary" : "outline"}
              onClick={() => setViewMode("list")}
            >
              List
            </Button>
          </div>
        </div>
      </div>
      <div className="col-span-12">
        {viewMode === "kanban" ? <KanbanBoard /> : <DealList />}
      </div>
    </div>
  );
}