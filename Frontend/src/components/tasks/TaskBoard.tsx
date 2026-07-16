// TaskBoard.tsx
"use client";
import React, { useState } from "react";
import Badge from "@/components/ui/badge/Badge";
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import { useTasks } from "@/hooks/useTasks";
import TaskService, { Task, TaskStatus } from "@/services/taskService";
import { useToast } from "@/context/ToastContext";

const STATUSES: TaskStatus[] = [
  "TODO",
  "IN_PROGRESS",
  "IN_REVIEW",
  "DONE"
];

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
  const { tasks, loading, error, refetch } = useTasks(1);
  const { addToast } = useToast();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);

  const handleCardClick = (task: Task) => {
    setSelectedTask(task);
    setIsEditModalOpen(true);
  };

  const handleCreate = () => {
    setSelectedTask(null);
    setIsCreateModalOpen(true);
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
    <div className="space-y-4">
      <div>
        <Button onClick={handleCreate}>New Task</Button>
      </div>
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
                  onClick={() => handleCardClick(task)}
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
    </div>
  );
}