"use client";
import React, { createContext, useContext, useState, useEffect, useCallback } from "react";

type ExtensionId = "gatekeeper" | "mailmine" | "dealfarm" | "taskfarm" | "filefarm" | "financefarm" | "inventoryfarm";

export type { ExtensionId };

interface ExtensionConfig {
  id: ExtensionId;
  name: string;
  path: string;
  enabled: boolean;
}

interface ExtensionContextType {
  extensions: Record<ExtensionId, ExtensionConfig>;
  isExtensionEnabled: (id: ExtensionId) => boolean;
  toggleExtension: (id: ExtensionId) => void;
}

const defaultExtensions: Record<ExtensionId, ExtensionConfig> = {
  gatekeeper: { id: "gatekeeper", name: "Gatekeeper", path: "/profile", enabled: true },
  mailmine: { id: "mailmine", name: "MailMine", path: "/email", enabled: true },
  dealfarm: { id: "dealfarm", name: "DealPick", path: "/dealfarm", enabled: true },
  taskfarm: { id: "taskfarm", name: "TaskPick", path: "/taskfarm", enabled: true },
  filefarm: { id: "filefarm", name: "FilePick", path: "/filefarm", enabled: true },
  financefarm: { id: "financefarm", name: "FinancePick", path: "/financefarm", enabled: true },
  inventoryfarm: { id: "inventoryfarm", name: "InventoryPick", path: "/inventoryfarm", enabled: true },
};

const ExtensionContext = createContext<ExtensionContextType | undefined>(undefined);

export const useExtensions = () => {
  const context = useContext(ExtensionContext);
  if (!context) {
    throw new Error("useExtensions must be used within ExtensionProvider");
  }
  return context;
};

export const ExtensionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [extensions, setExtensions] = useState<Record<ExtensionId, ExtensionConfig>>(defaultExtensions);

  useEffect(() => {
    try {
      const saved = localStorage.getItem("netpick-extensions");
      if (saved) {
        setExtensions(JSON.parse(saved));
      }
    } catch { /* ignore */ }
  }, []);

  useEffect(() => {
    try {
      localStorage.setItem("netpick-extensions", JSON.stringify(extensions));
    } catch { /* ignore */ }
  }, [extensions]);

  const isExtensionEnabled = useCallback((id: ExtensionId) => {
    return extensions[id]?.enabled ?? false;
  }, [extensions]);

  const toggleExtension = useCallback((id: ExtensionId) => {
    setExtensions(prev => ({
      ...prev,
      [id]: { ...prev[id], enabled: !prev[id].enabled }
    }));
  }, []);

  return (
    <ExtensionContext.Provider value={{ extensions, isExtensionEnabled, toggleExtension }}>
      {children}
    </ExtensionContext.Provider>
  );
};