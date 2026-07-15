"use client";

import React, { useState } from "react";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import TaskBoard from "@/components/tasks/TaskBoard";
import TaskList from "@/components/tasks/TaskList";
import Button from "@/components/ui/button/Button";

export default function TaskFarmPage() {
  const [viewMode, setViewMode] = useState<"board" | "list">("board");

  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <PageBreadcrumb pageTitle="TaskPick" />
        <div className="flex items-center justify-between mt-4">
          <p className="text-gray-500 dark:text-gray-400">
            Manage tasks, projects, and team collaboration
          </p>
          <div className="flex gap-2">
            <Button
              size="sm"
              variant={viewMode === "board" ? "primary" : "outline"}
              onClick={() => setViewMode("board")}
            >
              Board
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
        {viewMode === "board" ? <TaskBoard /> : <TaskList />}
      </div>
    </div>
  );
}