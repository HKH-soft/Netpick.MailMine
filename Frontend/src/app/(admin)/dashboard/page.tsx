"use client";
import { DashboardMetrics } from "@/components/dashboard/DashboardMetrics";
import RecentScrapeJobs from "@/components/dashboard/RecentScrapeJobs";
import RecentContacts from "@/components/dashboard/RecentContacts";
import { DashboardWidget } from "@/components/dashboard/DashboardWidget";
import { DashboardProvider, useDashboard } from "@/context/DashboardContext";
import { DndProvider } from "react-dnd";
import { HTML5Backend } from "react-dnd-html5-backend";
import React from "react";

function DashboardContent() {
  const { widgets, moveWidget, resizeWidget, removeWidget } = useDashboard();

  const sortedWidgets = [...widgets].sort((a, b) => a.position - b.position);

  const renderWidget = (widget: typeof widgets[0]) => {
    const widgetContent = {
      metrics: <DashboardMetrics />,
      "scrape-jobs": <RecentScrapeJobs />,
      contacts: <RecentContacts />,
    };

    return (
      <DashboardWidget
        key={widget.id}
        id={widget.id}
        title={widget.id === "metrics" ? "Overview" : widget.id === "scrape-jobs" ? "Recent Scrape Jobs" : "Recent Contacts"}
        colSpan={widget.colSpan}
        onRemove={removeWidget}
        onMove={moveWidget}
        onResize={resizeWidget}
        position={widget.position}
      >
        {widgetContent[widget.type as keyof typeof widgetContent]}
      </DashboardWidget>
    );
  };

  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      {sortedWidgets.filter(w => w.visible).map(renderWidget)}
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