import type { Metadata } from "next";
import React from "react";

export const metadata: Metadata = {
  title: "TaskPick - Tasks and Projects",
  description: "Manage tasks, projects, and team collaboration",
};

export default function TaskFarmPage() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <h1 className="text-2xl font-bold">TaskPick - Tasks and Projects</h1>
        <p className="text-gray-500 dark:text-gray-400 mt-2">
          Manage tasks, projects, and team collaboration
        </p>
      </div>
    </div>
  );
}