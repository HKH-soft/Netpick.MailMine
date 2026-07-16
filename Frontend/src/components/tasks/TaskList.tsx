// TaskList.tsx
"use client";
import React, { useState } from "react";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import Badge from "@/components/ui/badge/Badge";
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import ConfirmModal from "@/components/forms/ConfirmModal";
import { useTasks } from "@/hooks/useTasks";
import TaskService, { Task } from "@/services/taskService";
import { useToast } from "@/context/ToastContext";

const PRIORITIES = [
  { value: "LOW", label: "Low" },
  { value: "MEDIUM", label: "Medium" },
  { value: "HIGH", label: "High" },
  { value: "URGENT", label: "Urgent" },
];

const TASK_STATUSES = [
  { value: "TODO", label: "Todo" },
  { value: "IN_PROGRESS", label: "In Progress" },
  { value: "IN_REVIEW", label: "In Review" },
  { value: "DONE", label: "Done" },
];

const createFields = [
  { name: "title", label: "Title", type: "text", required: true },
  { name: "description", label: "Description", type: "textarea" },
  { name: "status", label: "Status", type: "select", required: true, options: TASK_STATUSES },
  { name: "priority", label: "Priority", type: "select", required: true, options: PRIORITIES },
  { name: "dueDate", label: "Due Date", type: "date" },
];

const editFields = [
  { name: "title", label: "Title", type: "text", required: true },
  { name: "description", label: "Description", type: "textarea" },
  { name: "status", label: "Status", type: "select", required: true, options: TASK_STATUSES },
  { name: "priority", label: "Priority", type: "select", required: true, options: PRIORITIES },
  { name: "dueDate", label: "Due Date", type: "date" },
];

export default function TaskList() {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const { addToast } = useToast();
  const { tasks, loading, error, totalPages, refetch } = useTasks(currentPage);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleCreate = () => {
    setSelectedTask(null);
    setIsCreateModalOpen(true);
  };

  const handleEdit = (row: Record<string, unknown>) => {
    const task = tasks.find(t => t.id === row.id);
    if (task) {
      setSelectedTask(task);
      setIsEditModalOpen(true);
    }
  };

  const handleDelete = (row: Record<string, unknown>) => {
    const task = tasks.find(t => t.id === row.id);
    if (task) {
      setSelectedTask(task);
      setIsDeleteModalOpen(true);
    }
  };

  const handleCreateSubmit = async (data: Record<string, unknown>) => {
    try {
      await TaskService.createTask({
        title: data.title as string,
        description: data.description as string,
        status: data.status as string,
        priority: data.priority as string,
        dueDate: data.dueDate as string,
      });
      addToast("success", "Success", "Task created successfully");
      setIsCreateModalOpen(false);
      await refetch();
    } catch (err) {
      console.error("Failed to create task:", err);
      addToast("error", "Error", "Failed to create task");
    }
  };

  const handleEditSubmit = async (data: Record<string, unknown>) => {
    if (!selectedTask) return;
    try {
      await TaskService.updateTask(selectedTask.id, {
        title: data.title as string,
        description: data.description as string,
        status: data.status as string,
        priority: data.priority as string,
        dueDate: data.dueDate as string,
      });
      addToast("success", "Success", "Task updated successfully");
      setIsEditModalOpen(false);
      await refetch();
    } catch (err) {
      console.error("Failed to update task:", err);
      addToast("error", "Error", "Failed to update task");
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
    { key: "edit", header: "Edit", type: "edit" },
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
      <div>
        <Button onClick={handleCreate}>New Task</Button>
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
        title="Create Task"
        fields={createFields}
        submitButtonText="Create"
      />
      <ModalForm
        isOpen={isEditModalOpen}
        onCloseAction={() => setIsEditModalOpen(false)}
        onSubmit={handleEditSubmit}
        title="Edit Task"
        fields={editFields}
        initialData={selectedTask ? {
          title: selectedTask.title,
          description: selectedTask.description || "",
          status: selectedTask.status,
          priority: selectedTask.priority,
          dueDate: selectedTask.dueDate?.split("T")[0] || "",
        } : undefined}
        submitButtonText="Update"
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