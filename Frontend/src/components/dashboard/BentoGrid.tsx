"use client";
import React from "react";

interface BentoGridProps {
  children: React.ReactNode;
  className?: string;
}

export default function BentoGrid({ children, className = "" }: BentoGridProps) {
  return (
    <div
      className={`grid grid-cols-1 sm:grid-cols-6 lg:grid-cols-12 gap-4 auto-rows-auto ${className}`}
      style={{ gridAutoFlow: "dense" }}
    >
      {children}
    </div>
  );
}
