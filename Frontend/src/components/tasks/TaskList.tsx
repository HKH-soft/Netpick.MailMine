// TaskList.tsx
"use client";
import React, { useState } from "react";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import Badge from "@/components/ui/badge/Badge";
import { useTasks } from "@/hooks/useTasks";
import TaskService from "@/services/taskService";
import { Task } from "@/services/taskService";
import { useToast } from "@/context/ToastContext";
import ConfirmModal from "@/components/forms/ConfirmModal";

export default function TaskList() {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const { addToast } = useToast();
  const { tasks, loading, error, totalPages, refetch } = useTasks(currentPage);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleDelete = (row: Record<string, unknown>) => {
    const task = tasks.find(t => t.id === row.id);
    if (task) {
      setSelectedTask(task);
      setIsDeleteModalOpen(true);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedTask) return;
    try {
      await TaskService.deleteTask(selectedTask.id);
      addToast("success", "Success", "Task deleted successfully");
      setIsDeleteModalOpen(false);
      await refetch();
    } catch (err) {
      console.error("Failed to delete task:", err);
      addToast("error", "Error", "Failed to delete task");
    }
  };

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

  const columns: ColumnConfig[] = [
    { key: "title", header: "Title", type: "text" },
    { 
      key: "status", 
      header: "Status", 
      type: "status",
      render: (value) => (
        <Badge size="sm" color={getPriorityColor(String(value))}>
          {String(value).replace("_", " ")}
        </Badge>
      )
    },
    { 
      key: "priority", 
      header: "Priority", 
      type: "status",
      render: (value) => (
        <Badge size="sm" color={getPriorityColor(String(value))}>
          {String(value)}
        </Badge>
      )
    },
    { key: "dueDate", header: "Due Date", type: "text", render: (value) => value ? new Date(String(value)).toLocaleDateString() : "-" },
    { key: "createdAt", header: "Created At", type: "text", render: (value) => value ? new Date(String(value)).toLocaleDateString() : "-" },
    { key: "delete", header: "Delete", type: "delete" },
  ];

  const data = tasks.map(task => ({
    id: task.id,
    title: task.title,
    status: task.status,
    priority: task.priority,
    dueDate: task.dueDate,
    createdAt: task.createdAt,
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
        title="Delete Task"
        message={`Are you sure you want to delete "${selectedTask?.title}"?`}
      />
    </div>
  );
}