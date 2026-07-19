"use client";
import React, { useState, useEffect } from "react";

interface PersonalizationSettings {
  density: "comfortable" | "compact";
  accentColor: string;
  glassIntensity: "none" | "low" | "medium" | "high";
}

const accentColors = [
  { name: "Green", value: "#22c55e" },
  { name: "Blue", value: "#3b82f6" },
  { name: "Purple", value: "#a855f7" },
  { name: "Orange", value: "#f97316" },
  { name: "Pink", value: "#ec4899" },
  { name: "Teal", value: "#14b8a6" },
];

interface DashboardPersonalizationProps {
  open: boolean;
  onClose: () => void;
}

export default function DashboardPersonalization({ open, onClose }: DashboardPersonalizationProps) {
  const [settings, setSettings] = useState<PersonalizationSettings>({
    density: "comfortable",
    accentColor: "#22c55e",
    glassIntensity: "medium",
  });

  useEffect(() => {
    try {
      const saved = localStorage.getItem("dashboard-personalization");
      if (saved) setSettings(JSON.parse(saved));
    } catch { /* ignore */ }
  }, []);

  useEffect(() => {
    try {
      localStorage.setItem("dashboard-personalization", JSON.stringify(settings));
      document.documentElement.style.setProperty("--color-accent", settings.accentColor);
      // Dispatch event for other components to listen and apply personalization
      window.dispatchEvent(new CustomEvent("personalizationChange", { detail: settings }));
    } catch { /* ignore */ }
  }, [settings]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[9998] flex justify-end" onClick={onClose}>
      <div className="fixed inset-0 bg-black/40 backdrop-blur-sm dark:bg-black/40" />
      <div
        className="relative w-full max-w-[380px] h-full border-l border-gray-200 dark:border-gray-800 overflow-y-auto bg-white dark:bg-gray-950 shadow-[-20px_0_60px_rgba(0,0,0,0.4)]"
        onClick={e => e.stopPropagation()}
      >
        <div className="sticky top-0 z-10 px-5 py-4 border-b border-gray-200 dark:border-gray-800 bg-white/95 dark:bg-gray-950/95 backdrop-blur-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-[15px] font-semibold text-gray-900 dark:text-white">Personalization</h2>
            <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 transition-colors">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <div className="p-5 space-y-8">
          {/* Density */}
          <div>
            <h3 className="text-[12px] font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-3">Density</h3>
            <div className="grid grid-cols-2 gap-2">
              {(["comfortable", "compact"] as const).map(d => (
                <button
                  key={d}
                  onClick={() => setSettings(prev => ({ ...prev, density: d }))}
                  className={`px-4 py-3 rounded-xl text-[13px] font-medium transition-all duration-200 border ${
                    settings.density === d
                      ? "bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400 border-green-200 dark:border-green-500/20"
                      : "bg-gray-50 dark:bg-gray-800 text-gray-500 dark:text-gray-400 border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600 hover:text-gray-700 dark:hover:text-gray-300"
                  }`}
                >
                  {d.charAt(0).toUpperCase() + d.slice(1)}
                </button>
              ))}
            </div>
          </div>

          {/* Accent Color */}
          <div>
            <h3 className="text-[12px] font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-3">Accent Color</h3>
            <div className="flex gap-3">
              {accentColors.map(color => (
                <button
                  key={color.value}
                  onClick={() => setSettings(prev => ({ ...prev, accentColor: color.value }))}
                  className={`w-9 h-9 rounded-full transition-all duration-200 ${
                    settings.accentColor === color.value
                      ? "ring-2 ring-offset-2 ring-offset-white dark:ring-offset-gray-950"
                      : "hover:scale-110"
                  }`}
                  style={{
                    background: color.value,
                    boxShadow: settings.accentColor === color.value ? `0 0 0 2px ${color.value}` : undefined,
                  }}
                  title={color.name}
                />
              ))}
            </div>
          </div>

          {/* Glass Intensity */}
          <div>
            <h3 className="text-[12px] font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-3">Glass Intensity</h3>
            <div className="space-y-1.5">
              {(["none", "low", "medium", "high"] as const).map(level => (
                <button
                  key={level}
                  onClick={() => setSettings(prev => ({ ...prev, glassIntensity: level }))}
                  className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-[13px] font-medium transition-all duration-200 border ${
                    settings.glassIntensity === level
                      ? "bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400 border-green-200 dark:border-green-500/20"
                      : "bg-gray-50 dark:bg-gray-800 text-gray-500 dark:text-gray-400 border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600 hover:text-gray-700 dark:hover:text-gray-300"
                  }`}
                >
                  <div
                    className="w-6 h-6 rounded-md border border-gray-200 dark:border-gray-700"
                    style={{
                      background: level === "none" ? "rgba(0,0,0,0.02)"
                        : level === "low" ? "rgba(0,0,0,0.04)"
                        : level === "medium" ? "rgba(0,0,0,0.07)"
                        : "rgba(0,0,0,0.1)",
                      backdropFilter: level === "none" ? "none"
                        : level === "low" ? "blur(4px)"
                        : level === "medium" ? "blur(8px)"
                        : "blur(16px)",
                    }}
                  />
                  {level.charAt(0).toUpperCase() + level.slice(1)}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
