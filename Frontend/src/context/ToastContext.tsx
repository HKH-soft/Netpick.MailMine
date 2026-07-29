"use client";

import React, { createContext, useContext, useState, useRef, useEffect, useCallback, ReactNode } from "react";
import Toast from "@/components/ui/toast/Toast";

type ToastType = "success" | "error" | "warning" | "info";

interface ToastItem {
  id: string;
  type: ToastType;
  title: string;
  message: string;
}

interface ToastContextType {
  addToast: (type: ToastType, title: string, message: string) => void;
  removeToast: (id: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return context;
};

export const ToastProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<ToastItem[]>([]);
  const timersRef = useRef<Map<string, ReturnType<typeof setTimeout>>>(new Map());

  const removeToast = useCallback((id: string) => {
    const timer = timersRef.current.get(id);
    if (timer) {
      clearTimeout(timer);
      timersRef.current.delete(id);
    }
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  const addToast = useCallback((type: ToastType, title: string, message: string) => {
    const id = typeof window !== 'undefined' ? crypto.randomUUID() : Math.random().toString(36).substring(2, 9);
    const newToast: ToastItem = { id, type, title, message };

    setToasts((prev) => [...prev, newToast]);

    const timer = setTimeout(() => {
      removeToast(id);
    }, 5000);
    timersRef.current.set(id, timer);
  }, [removeToast]);

  // Cleanup all timers on unmount
  useEffect(() => {
    const timers = timersRef.current;
    return () => {
      timers.forEach((timer) => clearTimeout(timer));
      timers.clear();
    };
  }, []);

  return (
    <ToastContext.Provider value={{ addToast, removeToast }}>
      {children}
      <div className="fixed top-20 right-4 z-[9999] space-y-2" role="log" aria-label="Notifications">
        {toasts.map((toast) => (
          <Toast
            key={toast.id}
            type={toast.type}
            title={toast.title}
            message={toast.message}
            onClose={() => removeToast(toast.id)}
          />
        ))}
      </div>
    </ToastContext.Provider>
  );
};


