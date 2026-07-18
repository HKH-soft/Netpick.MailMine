// TaskBoard.tsx
"use client";
import React, { useState } from "react";
import Badge from "@/components/ui/badge/Badge";
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import { useDrag, useDrop } from "react-dnd";
import { useTasks } from "@/hooks/useTasks";
import TaskService, { Task, TaskStatus } from "@/services/taskService";
import labelService from "@/services/labelService";
import { useToast } from "@/context/ToastContext";

const STATUSES: TaskStatus[] = [
  "TODO",
  "IN_PROGRESS",
  "IN_REVIEW",
  "DONE",
  "BLOCKED"
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
  { value: "BLOCKED", label: "Blocked" },
];

const LABEL_COLORS = [
  { value: "#EF4444", label: "Red" },
  { value: "#F59E0B", label: "Orange" },
  { value: "#10B981", label: "Green" },
  { value: "#3B82F6", label: "Blue" },
  { value: "#8B5CF6", label: "Purple" },
  { value: "#EC4899", label: "Pink" },
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
    case "BLOCKED":
      return "error";
    default:
      return "light";
  }
};

interface TaskCardProps {
  task: Task;
  index: number;
  status: TaskStatus;
  onCardClick: (task: Task) => void;
}

const TaskCard: React.FC<TaskCardProps> = ({ task, index, status, onCardClick }) => {
  const [, ref] = useDrag({
    type: "TASK",
    item: { id: task.id, status, index },
  });

  const getLabelColor = (color: string) => {
    const contrast = (parseInt(color.slice(1), 16) > 0xffffff / 2) ? "white" : "black";
    return { backgroundColor: color, color: contrast };
  };

  return (
    <div
      ref={ref as unknown as React.RefObject<HTMLDivElement>}
      className="bg-white dark:bg-white/[0.05] rounded-lg p-3 shadow-sm border border-gray-200 dark:border-white/[0.08] cursor-pointer hover:shadow-md transition-shadow"
      onClick={() => onCardClick(task)}
    >
      <h4 className="font-medium text-gray-800 dark:text-white/90 mb-2">
        {task.title}
      </h4>
      {task.labels && task.labels.length > 0 && (
        <div className="flex flex-wrap gap-1 mb-2">
          {task.labels.map(label => (
            <span
              key={label.id}
              className="text-xs px-2 py-0.5 rounded"
              style={getLabelColor(label.color)}
            >
              {label.name}
            </span>
          ))}
        </div>
      )}
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
  );
};

interface StatusColumnProps {
  status: TaskStatus;
  tasks: Task[];
  onCardClick: (task: Task) => void;
  onMove: (taskId: string, status: string, order: number) => void;
}

const StatusColumn: React.FC<StatusColumnProps> = ({ status, tasks, onCardClick, onMove }) => {
  const [, ref] = useDrop({
    accept: "TASK",
    drop: (item: { id: string; status: string; index: number }) => {
      const newStatus = status;
      const newOrder = tasks.length;
      if (item.status !== newStatus) {
        onMove(item.id, newStatus, newOrder);
      }
    },
  });

  return (
    <div ref={ref as unknown as React.RefObject<HTMLDivElement>} className="bg-gray-50 dark:bg-white/[0.03] rounded-lg p-4 min-w-[250px]">
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-semibold text-gray-800 dark:text-white/90">
          {status.replace("_", " ")}
        </h3>
        <Badge size="sm" color={getStatusColor(status)}>
          {tasks.length}
        </Badge>
      </div>
      <div className="space-y-3">
        {tasks.map((task, index) => (
          <TaskCard
            key={task.id}
            task={task}
            index={index}
            status={status}
            onCardClick={onCardClick}
          />
        ))}
        {tasks.length === 0 && (
          <div className="text-center text-gray-400 text-sm py-4">
            No tasks in this status
          </div>
        )}
      </div>
    </div>
  );
};

export default function TaskBoard() {
  const { tasks, refetch } = useTasks(1);
  const { addToast } = useToast();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isLabelModalOpen, setIsLabelModalOpen] = useState(false);
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
    } catch {
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
    } catch {
      addToast("error", "Error", "Failed to update task");
    }
  };

  const handleMove = async (taskId: string, status: string, order: number) => {
    try {
      await TaskService.reorderTask(taskId, status, order);
      await refetch();
    } catch {
      addToast("error", "Error", "Failed to move task");
    }
  };

  const tasksByStatus = STATUSES.reduce((acc, status) => {
    acc[status] = tasks.filter(task => task.status === status);
    return acc;
  }, {} as Record<string, Task[]>);

  return (
    <div className="space-y-4">
      <div className="flex gap-2">
        <Button onClick={handleCreate}>New Task</Button>
        <Button variant="outline" onClick={() => setIsLabelModalOpen(true)}>
          Manage Labels
        </Button>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 overflow-x-auto">
        {STATUSES.map(status => (
          <StatusColumn
            key={status}
            status={status}
            tasks={tasksByStatus[status]}
            onCardClick={handleCardClick}
            onMove={handleMove}
          />
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
      <ModalForm
        isOpen={isLabelModalOpen}
        onCloseAction={() => setIsLabelModalOpen(false)}
onSubmit={async (data) => {
           try {
             await labelService.createLabel({
               name: data.name as string,
               color: data.color as string,
               projectId: data.projectId as string,
               createdById: data.createdById as string,
             });
             addToast("success", "Success", "Label created");
             setIsLabelModalOpen(false);
           } catch {
             addToast("error", "Error", "Failed to create label");
           }
         }}
        title="Create Label"
        fields={[
          { name: "name", label: "Label Name", type: "text", required: true },
          { name: "color", label: "Color", type: "select", options: LABEL_COLORS, required: true },
          { name: "projectId", label: "Project ID", type: "text" },
          { name: "createdById", label: "Creator ID", type: "text" },
        ]}
        submitButtonText="Create"
      />
    </div>
  );
}