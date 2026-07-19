"use client";
import { DashboardMetrics } from "@/components/dashboard/DashboardMetrics";
import RecentScrapeJobs from "@/components/dashboard/RecentScrapeJobs";
import RecentContacts from "@/components/dashboard/RecentContacts";
import DashboardWidget from "@/components/dashboard/DashboardWidget";
import QuickActionsWidget from "@/components/dashboard/QuickActionsWidget";
import ActivityWidget from "@/components/dashboard/ActivityWidget";
import ChartWidget from "@/components/dashboard/ChartWidget";
import CommandPalette from "@/components/dashboard/CommandPalette";
import WidgetLibrary from "@/components/dashboard/WidgetLibrary";
import DashboardPersonalization from "@/components/dashboard/DashboardPersonalization";
import BentoGrid from "@/components/dashboard/BentoGrid";
import { DashboardProvider, useDashboard } from "@/context/DashboardContext";
import { DndProvider } from "react-dnd";
import { HTML5Backend } from "react-dnd-html5-backend";
import React, { useState } from "react";

const widgetComponents: Record<string, React.FC> = {
  metrics: DashboardMetrics,
  chart: ChartWidget,
  "recent-jobs": RecentScrapeJobs,
  "recent-contacts": RecentContacts,
  "quick-actions": QuickActionsWidget,
  activity: ActivityWidget,
};

function DashboardContent() {
  const { widgets, editMode, setEditMode, setLibraryOpen, removeWidget, moveWidget, resizeWidget, collapseWidget, pinWidget } = useDashboard();
  const [personalizationOpen, setPersonalizationOpen] = useState(false);

  const sortedWidgets = [...widgets].sort((a, b) => a.position - b.position);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900 dark:text-white tracking-tight">Dashboard</h1>
          <p className="text-[13px] text-gray-500 dark:text-gray-400 mt-0.5">Your workspace overview</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setPersonalizationOpen(true)}
            className="px-4 py-2 text-[12px] font-medium rounded-xl border border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-900 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 hover:border-gray-300 dark:hover:border-gray-700 transition-all duration-200"
          >
            Theme
          </button>
          <button
            onClick={() => setLibraryOpen(true)}
            className="px-4 py-2 text-[12px] font-medium rounded-xl border border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-900 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 hover:border-gray-300 dark:hover:border-gray-700 transition-all duration-200"
          >
            Customize
          </button>
          <button
            onClick={() => setEditMode(!editMode)}
            className={`px-4 py-2 text-[12px] font-medium rounded-xl border transition-all duration-200 ${
              editMode
                ? "bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400 border-green-200 dark:border-green-500/20 hover:bg-green-100 dark:hover:bg-green-900/30"
                : "border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-900 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800"
            }`}
          >
            {editMode ? "Done Editing" : "Edit Layout"}
          </button>
        </div>
      </div>

      <BentoGrid>
        {sortedWidgets.filter(w => w.visible).map(widget => {
          const Component = widgetComponents[widget.type];
          return (
            <DashboardWidget
              key={widget.id}
              id={widget.id}
              title={widget.title}
              colSpan={widget.colSpan}
              rowSpan={widget.rowSpan}
              collapsed={widget.collapsed}
              pinned={widget.pinned}
              editMode={editMode}
              position={widget.position}
              onRemove={removeWidget}
              onMove={moveWidget}
              onResize={resizeWidget}
              onCollapse={collapseWidget}
              onPin={pinWidget}
            >
              {Component ? <Component /> : (
                <div className="flex items-center justify-center h-32 text-gray-400 dark:text-gray-500 text-[13px]">
                  Widget not available
                </div>
              )}
            </DashboardWidget>
          );
        })}
      </BentoGrid>

      <WidgetLibrary />
      <DashboardPersonalization open={personalizationOpen} onClose={() => setPersonalizationOpen(false)} />
      <CommandPalette />
    </div>
  );
}

export default function Dashboard() {
  return (
    <DndProvider backend={HTML5Backend}>
      <DashboardProvider>
        <DashboardContent />
      </DashboardProvider>
    </DndProvider>
  );
}
