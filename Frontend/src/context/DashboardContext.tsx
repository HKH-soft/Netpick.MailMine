"use client";
import React, { createContext, useContext, useState, useEffect, useCallback } from "react";

export interface WidgetType {
  name: string;
  description: string;
  icon: string;
  category: string;
  defaultColSpan: number;
  defaultRowSpan: number;
  minWidth: number;
}

export interface WidgetConfig {
  id: string;
  type: string;
  position: number;
  colSpan: number;
  rowSpan: number;
  visible: boolean;
  collapsed: boolean;
  pinned: boolean;
  title: string;
}

export interface DashboardLayout {
  name: string;
  widgets: WidgetConfig[];
  createdAt: string;
}

type DashboardContextType = {
  widgets: WidgetConfig[];
  widgetRegistry: Map<string, WidgetType>;
  editMode: boolean;
  libraryOpen: boolean;
  activeLayout: string;
  savedLayouts: string[];
  addWidget: (type: string, title?: string) => void;
  removeWidget: (id: string) => void;
  moveWidget: (fromIndex: number, toIndex: number) => void;
  resizeWidget: (id: string, colSpan: number) => void;
  collapseWidget: (id: string) => void;
  pinWidget: (id: string) => void;
  toggleVisibility: (id: string) => void;
  resetLayout: () => void;
  setEditMode: (edit: boolean) => void;
  setLibraryOpen: (open: boolean) => void;
  saveLayout: (name: string) => void;
  loadLayout: (name: string) => void;
  deleteLayout: (name: string) => void;
  registerWidget: (type: string, config: WidgetType) => void;
};

const widgetRegistry = new Map<string, WidgetType>([
  ["metrics", { name: "KPI Cards", description: "Key performance indicators at a glance", icon: "chart", category: "Analytics", defaultColSpan: 12, defaultRowSpan: 1, minWidth: 3 }],
  ["chart", { name: "Revenue Chart", description: "Interactive analytics chart", icon: "bar-chart", category: "Analytics", defaultColSpan: 8, defaultRowSpan: 2, minWidth: 4 }],
  ["recent-jobs", { name: "Recent Jobs", description: "Latest scrape job activity", icon: "list", category: "Data", defaultColSpan: 4, defaultRowSpan: 2, minWidth: 3 }],
  ["recent-contacts", { name: "Recent Contacts", description: "Newly discovered contacts", icon: "users", category: "Data", defaultColSpan: 6, defaultRowSpan: 1, minWidth: 3 }],
  ["quick-actions", { name: "Quick Actions", description: "Frequently used actions", icon: "zap", category: "Tools", defaultColSpan: 6, defaultRowSpan: 1, minWidth: 3 }],
  ["activity", { name: "Activity Feed", description: "Recent platform activity", icon: "activity", category: "Activity", defaultColSpan: 4, defaultRowSpan: 2, minWidth: 3 }],
]);

const defaultWidgets: WidgetConfig[] = [
  { id: "w-metrics", type: "metrics", position: 0, colSpan: 12, rowSpan: 1, visible: true, collapsed: false, pinned: true, title: "Overview" },
  { id: "w-chart", type: "chart", position: 1, colSpan: 8, rowSpan: 2, visible: true, collapsed: false, pinned: false, title: "Analytics" },
  { id: "w-activity", type: "activity", position: 2, colSpan: 4, rowSpan: 2, visible: true, collapsed: false, pinned: false, title: "Activity" },
  { id: "w-quick-actions", type: "quick-actions", position: 3, colSpan: 6, rowSpan: 1, visible: true, collapsed: false, pinned: false, title: "Quick Actions" },
  { id: "w-contacts", type: "recent-contacts", position: 4, colSpan: 6, rowSpan: 1, visible: true, collapsed: false, pinned: false, title: "Recent Contacts" },
  { id: "w-jobs", type: "recent-jobs", position: 5, colSpan: 6, rowSpan: 1, visible: true, collapsed: false, pinned: false, title: "Recent Jobs" },
];

const DashboardContext = createContext<DashboardContextType | undefined>(undefined);

export const useDashboard = () => {
  const context = useContext(DashboardContext);
  if (!context) throw new Error("useDashboard must be used within DashboardProvider");
  return context;
};

export const DashboardProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [widgets, setWidgets] = useState<WidgetConfig[]>(defaultWidgets);
  const [registry, setRegistry] = useState(new Map(widgetRegistry));
  const [editMode, setEditMode] = useState(false);
  const [libraryOpen, setLibraryOpen] = useState(false);
  const [activeLayout, setActiveLayout] = useState("default");
  const [savedLayouts, setSavedLayouts] = useState<string[]>(["default"]);

  useEffect(() => {
    try {
      const saved = localStorage.getItem("dashboard-layout-v2");
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.widgets?.length) setWidgets(parsed.widgets);
        if (parsed.activeLayout) setActiveLayout(parsed.activeLayout);
        if (parsed.savedLayouts) setSavedLayouts(parsed.savedLayouts);
      }
    } catch { /* ignore */ }
  }, []);

  useEffect(() => {
    try {
      localStorage.setItem("dashboard-layout-v2", JSON.stringify({ widgets, activeLayout, savedLayouts }));
    } catch { /* ignore */ }
  }, [widgets, activeLayout, savedLayouts]);

  const registerWidget = useCallback((type: string, config: WidgetType) => {
    setRegistry(prev => {
      const next = new Map(prev);
      next.set(type, config);
      return next;
    });
  }, []);

  const addWidget = useCallback((type: string, title?: string) => {
    const meta = registry.get(type);
    if (!meta) return;
    setWidgets(prev => {
      const newWidget: WidgetConfig = {
        id: `w-${type}-${Date.now()}`,
        type,
        position: prev.length,
        colSpan: meta.defaultColSpan,
        rowSpan: meta.defaultRowSpan,
        visible: true,
        collapsed: false,
        pinned: false,
        title: title || meta.name,
      };
      return [...prev, newWidget];
    });
  }, [registry]);

  const removeWidget = useCallback((id: string) => {
    setWidgets(prev => prev.filter(w => w.id !== id));
  }, []);

  const moveWidget = useCallback((fromIndex: number, toIndex: number) => {
    setWidgets(prev => {
      const arr = [...prev];
      const [moved] = arr.splice(fromIndex, 1);
      arr.splice(toIndex, 0, moved);
      return arr.map((w, i) => ({ ...w, position: i }));
    });
  }, []);

  const resizeWidget = useCallback((id: string, colSpan: number) => {
    setWidgets(prev => prev.map(w => w.id === id ? { ...w, colSpan: Math.max(1, Math.min(12, colSpan)) } : w));
  }, []);

  const collapseWidget = useCallback((id: string) => {
    setWidgets(prev => prev.map(w => w.id === id ? { ...w, collapsed: !w.collapsed } : w));
  }, []);

  const pinWidget = useCallback((id: string) => {
    setWidgets(prev => prev.map(w => w.id === id ? { ...w, pinned: !w.pinned } : w));
  }, []);

  const toggleVisibility = useCallback((id: string) => {
    setWidgets(prev => prev.map(w => w.id === id ? { ...w, visible: !w.visible } : w));
  }, []);

  const resetLayout = useCallback(() => {
    setWidgets(defaultWidgets);
    setActiveLayout("default");
  }, []);

  const saveLayout = useCallback((name: string) => {
    setSavedLayouts(prev => prev.includes(name) ? prev : [...prev, name]);
    setActiveLayout(name);
  }, []);

  const loadLayout = useCallback((name: string) => {
    try {
      const saved = localStorage.getItem(`dashboard-layout-${name}`);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.widgets?.length) {
          setWidgets(parsed.widgets);
          setActiveLayout(name);
        }
      }
    } catch { /* ignore */ }
  }, []);

  const deleteLayout = useCallback((name: string) => {
    if (name === "default") return;
    setSavedLayouts(prev => prev.filter(n => n !== name));
    localStorage.removeItem(`dashboard-layout-${name}`);
    if (activeLayout === name) {
      setActiveLayout("default");
      setWidgets(defaultWidgets);
    }
  }, [activeLayout]);

  return (
    <DashboardContext.Provider value={{
      widgets, widgetRegistry: registry, editMode, libraryOpen, activeLayout, savedLayouts,
      addWidget, removeWidget, moveWidget, resizeWidget, collapseWidget, pinWidget,
      toggleVisibility, resetLayout, setEditMode, setLibraryOpen, saveLayout, loadLayout,
      deleteLayout, registerWidget,
    }}>
      {children}
    </DashboardContext.Provider>
  );
};
