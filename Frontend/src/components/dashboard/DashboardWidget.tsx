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
   const lastMovedPositionRef = useRef<number | null>(null);

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
       // Prevent jittering by only moving when we've crossed to a new position
       if (lastMovedPositionRef.current !== position) {
         onMove(item.position, position);
         item.position = position;
         lastMovedPositionRef.current = position;
       }
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
        ${editMode ? "ring-1 ring-gray-200 dark:ring-gray-800" : ""}
        bg-gray-50 dark:bg-gray-900
        border-gray-200 dark:border-gray-800
        hover:bg-gray-100 dark:hover:bg-gray-800
        hover:border-gray-300 dark:hover:border-gray-700
      `}
    >
      {/* Title bar */}
      <div className="flex items-center justify-between px-5 py-3.5 border-b border-gray-200 dark:border-gray-800">
        <div className="flex items-center gap-2.5">
          {editMode && (
            <div className="cursor-grab active:cursor-grabbing p-1 -ml-1 rounded hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
              <svg className="w-3.5 h-3.5 text-gray-400 dark:text-gray-500" viewBox="0 0 16 16" fill="currentColor">
                <circle cx="5" cy="3" r="1.5"/><circle cx="11" cy="3" r="1.5"/>
                <circle cx="5" cy="8" r="1.5"/><circle cx="11" cy="8" r="1.5"/>
                <circle cx="5" cy="13" r="1.5"/><circle cx="11" cy="13" r="1.5"/>
              </svg>
            </div>
          )}
          {icon && <span className="text-gray-400 dark:text-gray-500">{icon}</span>}
          <h3 className="text-[13px] font-semibold text-gray-900 dark:text-white tracking-tight">{title}</h3>
          {pinned && (
            <svg className="w-3 h-3 text-green-500" fill="currentColor" viewBox="0 0 24 24">
              <path d="M16 12V4h1V2H7v2h1v8l-2 2v2h5.2v6h1.6v-6H18v-2l-2-2z"/>
            </svg>
          )}
        </div>
        {editMode && (
          <div className="flex items-center gap-1">
            {onCollapse && (
              <button onClick={() => onCollapse(id)} className="p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 hover:text-gray-900 dark:hover:text-white transition-colors" title={collapsed ? "Expand" : "Collapse"}>
                <svg className={`w-3.5 h-3.5 transition-transform ${collapsed ? "rotate-180" : ""}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7"/>
                </svg>
              </button>
            )}
            {onPin && (
              <button onClick={() => onPin(id)} className={`p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors ${pinned ? "text-green-500" : "text-gray-400 dark:text-gray-500 hover:text-gray-900 dark:hover:text-white"}`} title="Pin">
                <svg className="w-3.5 h-3.5" fill={pinned ? "currentColor" : "none"} stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 12V4h1V2H7v2h1v8l-2 2v2h5.2v6h1.6v-6H18v-2l-2-2z"/>
                </svg>
              </button>
            )}
            {onResize && (
              <button onClick={cycleSize} className="p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 dark:text-gray-500 hover:text-gray-900 dark:hover:text-white transition-colors" title="Resize">
                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4"/>
                </svg>
              </button>
            )}
            {onRemove && (
              <button onClick={() => onRemove(id)} className="p-1.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-900/20 text-gray-400 dark:text-gray-500 hover:text-red-500 dark:hover:text-red-400 transition-colors" title="Remove">
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
