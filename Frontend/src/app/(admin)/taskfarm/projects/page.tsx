"use client";

import React, { useState } from "react";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import Button from "@/components/ui/button/Button";
import { DndProvider } from "react-dnd";
import { HTML5Backend } from "react-dnd-html5-backend";

export default function ProjectsPage() {
  const [projects] = useState([
    { id: "1", name: "Website Redesign", progress: 65, dueDate: "2024-02-15" },
    { id: "2", name: "Mobile App Launch", progress: 30, dueDate: "2024-03-01" },
    { id: "3", name: "Q1 Marketing Campaign", progress: 90, dueDate: "2024-01-31" },
  ]);

  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <PageBreadcrumb pageTitle="Projects" />
        <div className="flex items-center justify-between mt-4">
          <p className="text-gray-500 dark:text-gray-400">
            Manage your projects and track progress
          </p>
          <Button size="sm">New Project</Button>
        </div>
      </div>

      <div className="col-span-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {projects.map((project) => (
            <div
              key={project.id}
              className="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-5"
            >
              <h3 className="text-[15px] font-semibold text-gray-900 dark:text-white mb-2">
                {project.name}
              </h3>
              <div className="w-full h-2 bg-gray-100 dark:bg-gray-800 rounded-full mb-2">
                <div
                  className="h-full bg-brand-500 rounded-full"
                  style={{ width: `${project.progress}%` }}
                ></div>
              </div>
              <div className="flex justify-between text-[12px] text-gray-500 dark:text-gray-400">
                <span>{project.progress}% complete</span>
                <span>Due: {project.dueDate}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}