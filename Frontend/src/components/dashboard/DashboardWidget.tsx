"use client";
import React, { useRef, useState } from "react";
import { useDrag, useDrop } from "react-dnd";

interface DashboardWidgetProps {
  id: string;
  title: string;
  icon?: React.ReactNode;
  children: React.ReactNode;
  colSpan?: number;
  rowSpan?: number;
  collapsed?: boolean;
  pinned?: boolean;
  editMode?: boolean;
  onRemove?: (id: string) => void;
  onMove?: (from: number, to: number) => void;
  onResize?: (id: string, colSpan: number) => void;
  onCollapse?: (id: string) => void;
  onPin?: (id: string) => void;
  position?: number;
}

interface DragItem {
  id: string;
  type: string;
  position: number;
}

const ROW_CLASSES: Record<number, string> = {
  1: "row-span-1", 2: "row-span-2", 3: "row-span-3",
};

const RESPONSIVE_SPAN: Record<number, string> = {
  1: "col-span-1 sm:col-span-3 lg:col-span-1",
  2: "col-span-1 sm:col-span-3 lg:col-span-2",
  3: "col-span-1 sm:col-span-3 lg:col-span-3",
  4: "col-span-1 sm:col-span-3 lg:col-span-4",
  5: "col-span-1 sm:col-span-3 lg:col-span-5",
  6: "col-span-1 sm:col-span-6 lg:col-span-6",
  7: "col-span-1 sm:col-span-6 lg:col-span-7",
  8: "col-span-1 sm:col-span-6 lg:col-span-8",
  9: "col-span-1 sm:col-span-6 lg:col-span-9",
  10: "col-span-1 sm:col-span-6 lg:col-span-10",
  11: "col-span-1 sm:col-span-6 lg:col-span-11",
  12: "col-span-1 sm:col-span-6 lg:col-span-12",
};

export default function DashboardWidget({
  id, title, icon, children, colSpan = 6, rowSpan = 1,
  collapsed = false, pinned = false, editMode = false,
  onRemove, onMove, onResize, onCollapse, onPin, position = 0,
}: DashboardWidgetProps) {
  const ref = useRef<HTMLDivElement>(null);
  const [hovered, setHovered] = useState(false);

  const [{ isDragging }, drag] = useDrag({
    type: "WIDGET",
    item: { id, type: "WIDGET", position },
    canDrag: editMode,
    collect: (monitor) => ({ isDragging: monitor.isDragging() }),
  });

  const [, drop] = useDrop({
    accept: "WIDGET",
    hover: (item: DragItem) => {
      if (!editMode || item.position === position || !onMove) return;
      onMove(item.position, position);
      item.position = position;
    },
  });

  drag(drop(ref));

  const cycleSize = () => {
    if (!onResize) return;
    const sizes = [12, 6, 4, 3];
    const idx = sizes.indexOf(colSpan);
    onResize(id, sizes[(idx + 1) % sizes.length]);
  };

  return (
    <div
      ref={ref}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      className={`
        ${RESPONSIVE_SPAN[colSpan] || "col-span-12"}
        ${ROW_CLASSES[rowSpan] || "row-span-1"}
        rounded-2xl border transition-all duration-300
        ${isDragging ? "opacity-40 scale-95" : "opacity-100"}
        ${editMode ? "ring-1 ring-white/[0.04]" : ""}
      `}
      style={{
        background: hovered
          ? "linear-gradient(180deg, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0.025) 100%)"
          : "linear-gradient(180deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0.015) 100%)",
        borderColor: hovered ? "rgba(255,255,255,0.1)" : "rgba(255,255,255,0.06)",
        boxShadow: hovered
          ? "0 4px 24px rgba(0,0,0,0.25), 0 0 0 1px rgba(34,197,94,0.03)"
          : "0 1px 2px rgba(0,0,0,0.1)",
        minHeight: rowSpan === 2 ? "320px" : "auto",
      }}
    >
      {/* Title bar */}
      <div className="flex items-center justify-between px-5 py-3.5 border-b border-white/[0.04]">
        <div className="flex items-center gap-2.5">
          {editMode && (
            <div className="cursor-grab active:cursor-grabbing p-1 -ml-1 rounded hover:bg-white/[0.05] transition-colors">
              <svg className="w-3.5 h-3.5 text-white/30" viewBox="0 0 16 16" fill="currentColor">
                <circle cx="5" cy="3" r="1.5"/><circle cx="11" cy="3" r="1.5"/>
                <circle cx="5" cy="8" r="1.5"/><circle cx="11" cy="8" r="1.5"/>
                <circle cx="5" cy="13" r="1.5"/><circle cx="11" cy="13" r="1.5"/>
              </svg>
            </div>
          )}
          {icon && <span className="text-white/40">{icon}</span>}
          <h3 className="text-[13px] font-semibold text-white/80 tracking-tight">{title}</h3>
          {pinned && (
            <svg className="w-3 h-3 text-green-500/60" fill="currentColor" viewBox="0 0 24 24">
              <path d="M16 12V4h1V2H7v2h1v8l-2 2v2h5.2v6h1.6v-6H18v-2l-2-2z"/>
            </svg>
          )}
        </div>
        {editMode && (
          <div className="flex items-center gap-1">
            {onCollapse && (
              <button onClick={() => onCollapse(id)} className="p-1.5 rounded-lg hover:bg-white/[0.06] text-white/30 hover:text-white/60 transition-colors" title={collapsed ? "Expand" : "Collapse"}>
                <svg className={`w-3.5 h-3.5 transition-transform ${collapsed ? "rotate-180" : ""}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7"/>
                </svg>
              </button>
            )}
            {onPin && (
              <button onClick={() => onPin(id)} className={`p-1.5 rounded-lg hover:bg-white/[0.06] transition-colors ${pinned ? "text-green-500/60" : "text-white/30 hover:text-white/60"}`} title="Pin">
                <svg className="w-3.5 h-3.5" fill={pinned ? "currentColor" : "none"} stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 12V4h1V2H7v2h1v8l-2 2v2h5.2v6h1.6v-6H18v-2l-2-2z"/>
                </svg>
              </button>
            )}
            {onResize && (
              <button onClick={cycleSize} className="p-1.5 rounded-lg hover:bg-white/[0.06] text-white/30 hover:text-white/60 transition-colors" title="Resize">
                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4"/>
                </svg>
              </button>
            )}
            {onRemove && (
              <button onClick={() => onRemove(id)} className="p-1.5 rounded-lg hover:bg-red-500/10 text-white/30 hover:text-red-400 transition-colors" title="Remove">
                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            )}
          </div>
        )}
      </div>

      {/* Content */}
      <div className={`p-5 transition-all duration-300 ${collapsed ? "h-0 p-0 overflow-hidden" : ""}`}>
        {children}
      </div>
    </div>
  );
}
