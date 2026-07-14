"use client";
import React, { createContext, useContext, useState, useEffect } from "react";

export interface WidgetConfig {
  id: string;
  type: "metrics" | "scrape-jobs" | "contacts";
  position: number;
  colSpan: number;
  visible: boolean;
}

type DashboardContextType = {
  widgets: WidgetConfig[];
  moveWidget: (id: string, newPosition: number) => void;
  resizeWidget: (id: string, colSpan: number) => void;
  removeWidget: (id: string) => void;
  resetLayout: () => void;
};

const defaultWidgets: WidgetConfig[] = [
  { id: "metrics", type: "metrics", position: 0, colSpan: 12, visible: true },
  { id: "scrape-jobs", type: "scrape-jobs", position: 1, colSpan: 6, visible: true },
  { id: "contacts", type: "contacts", position: 2, colSpan: 6, visible: true },
];

const DashboardContext = createContext<DashboardContextType | undefined>(undefined);

export const useDashboard = () => {
  const context = useContext(DashboardContext);
  if (!context) {
    throw new Error("useDashboard must be used within a DashboardProvider");
  }
  return context;
};

export const DashboardProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [widgets, setWidgets] = useState<WidgetConfig[]>(() => {
    // Load from localStorage if available
    if (typeof window !== "undefined") {
      const saved = localStorage.getItem("dashboard-widgets");
      return saved ? JSON.parse(saved) : defaultWidgets;
    }
    return defaultWidgets;
  });

  useEffect(() => {
    localStorage.setItem("dashboard-widgets", JSON.stringify(widgets));
  }, [widgets]);

  const moveWidget = (id: string, newPosition: number) => {
    setWidgets((prev) => {
      const newWidgets = [...prev];
      const widgetIndex = newWidgets.findIndex((w) => w.id === id);
      if (widgetIndex !== -1) {
        const [widget] = newWidgets.splice(widgetIndex, 1);
        newWidgets.splice(newPosition, 0, { ...widget, position: newPosition });
        // Update positions
        return newWidgets.map((w, i) => ({ ...w, position: i }));
      }
      return prev;
    });
  };

  const resizeWidget = (id: string, colSpan: number) => {
    setWidgets((prev) =>
      prev.map((w) => (w.id === id ? { ...w, colSpan } : w))
    );
  };

  const removeWidget = (id: string) => {
    setWidgets((prev) => prev.filter((w) => w.id !== id));
  };

  const resetLayout = () => {
    setWidgets(defaultWidgets);
  };

  return (
    <DashboardContext.Provider
      value={{ widgets, moveWidget, resizeWidget, removeWidget, resetLayout }}
    >
      {children}
    </DashboardContext.Provider>
  );
};