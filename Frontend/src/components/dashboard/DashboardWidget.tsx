"use client";
import React, { useRef } from "react";
import { useDrag, useDrop } from "react-dnd";
import { CloseIcon, GripVerticalIcon, ChevronDownSmIcon } from "@/icons";

interface DashboardWidgetProps {
  id: string;
  title: string;
  children: React.ReactNode;
  colSpan?: number;
  onRemove?: (id: string) => void;
  onMove?: (id: string, newPosition: number) => void;
  onResize?: (id: string, colSpan: number) => void;
  position: number;
}

interface DragItem {
  id: string;
  type: string;
  position: number;
}

const COL_SPANS = [12, 6, 4, 3];

export const DashboardWidget: React.FC<DashboardWidgetProps> = ({
  id,
  title,
  children,
  colSpan = 3,
  onRemove,
  onMove,
  onResize,
  position,
}) => {
  const resizeRef = useRef<HTMLDivElement>(null);

  const [, drag] = useDrag({
    type: "WIDGET",
    item: { id, type: "WIDGET", position },
  });

  const [, drop] = useDrop({
    accept: "WIDGET",
    hover: (item: DragItem) => {
      if (item.position !== position && onMove) {
        onMove(item.id, position);
      }
    },
  });

  const handleResizeClick = () => {
    if (!onResize) return;
    const currentIndex = COL_SPANS.indexOf(colSpan);
    const nextIndex = (currentIndex + 1) % COL_SPANS.length;
    onResize(id, COL_SPANS[nextIndex]);
  };

  const getWidthClass = () => {
    if (colSpan === 12) return "col-span-12";
    if (colSpan === 6) return "col-span-12 lg:col-span-6";
    if (colSpan === 4) return "col-span-12 lg:col-span-4";
    return "col-span-12 lg:col-span-3";
  };

  return (
    <div
      ref={drop}
      className={`rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03] ${getWidthClass()} relative`}
    >
      <div
        ref={drag}
        className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-800 cursor-move"
      >
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          {title}
        </h3>
        <div className="flex items-center gap-2">
          {onRemove && (
            <button
              onClick={() => onRemove(id)}
              className="p-1 text-gray-500 hover:text-error-500"
            >
              <CloseIcon className="w-4 h-4" />
            </button>
          )}
          <GripVerticalIcon className="w-4 h-4 text-gray-400" />
        </div>
      </div>
      <div className="p-4">{children}</div>
      {onResize && (
        <button
          ref={resizeRef}
          onClick={handleResizeClick}
          className="absolute bottom-2 right-2 p-1 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          title="Resize widget"
        >
          <ChevronDownSmIcon className="w-4 h-4 rotate-45" />
        </button>
      )}
    </div>
  );
};