// TaskBoard.tsx
"use client";
import React from "react";
import Badge from "@/components/ui/badge/Badge";
import { useTasks } from "@/hooks/useTasks";
import { TaskStatus } from "@/services/taskService";

const STATUSES: TaskStatus[] = [
  "TODO",
  "IN_PROGRESS",
  "IN_REVIEW",
  "DONE"
];

const getPriorityColor = (priority: string) => {
  switch (priority) {
    case "LOW":
      return "light";
    case "MEDIUM":
      return "info";
    case "HIGH":
      return "warning";
    case "URGENT":
      return "error";
    default:
      return "light";
  }
};

const getStatusColor = (status: string) => {
  switch (status) {
    case "TODO":
      return "light";
    case "IN_PROGRESS":
      return "info";
    case "IN_REVIEW":
      return "warning";
    case "DONE":
      return "success";
    default:
      return "light";
  }
};

export default function TaskBoard() {
  const { tasks, loading, error } = useTasks(1);

  if (loading) {
    return <div className="p-4">Loading...</div>;
  }

  if (error) {
    return <div className="p-4 text-red-500">Error: {error}</div>;
  }

  const tasksByStatus = STATUSES.reduce((acc, status) => {
    acc[status] = tasks.filter(task => task.status === status);
    return acc;
  }, {} as Record<string, typeof tasks>);

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 overflow-x-auto">
      {STATUSES.map(status => (
        <div key={status} className="bg-gray-50 dark:bg-white/[0.03] rounded-lg p-4 min-w-[250px]">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold text-gray-800 dark:text-white/90">
              {status.replace("_", " ")}
            </h3>
            <Badge size="sm" color={getStatusColor(status)}>
              {tasksByStatus[status].length}
            </Badge>
          </div>
          <div className="space-y-3">
            {tasksByStatus[status].map(task => (
              <div
                key={task.id}
                className="bg-white dark:bg-white/[0.05] rounded-lg p-3 shadow-sm border border-gray-200 dark:border-white/[0.08] cursor-pointer hover:shadow-md transition-shadow"
              >
                <h4 className="font-medium text-gray-800 dark:text-white/90 mb-2">
                  {task.title}
                </h4>
                <div className="flex items-center justify-between">
                  <Badge size="sm" color={getPriorityColor(task.priority)}>
                    {task.priority}
                  </Badge>
                  {task.dueDate && (
                    <span className="text-xs text-gray-400">
                      {new Date(task.dueDate).toLocaleDateString()}
                    </span>
                  )}
                </div>
              </div>
            ))}
            {tasksByStatus[status].length === 0 && (
              <div className="text-center text-gray-400 text-sm py-4">
                No tasks in this status
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}