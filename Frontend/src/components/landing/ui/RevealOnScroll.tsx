"use client";

import React from "react";
import { useScrollReveal } from "@/hooks/useScrollReveal";

interface RevealOnScrollProps {
  children: React.ReactNode;
  className?: string;
  delay?: number;
  direction?: "up" | "down" | "left" | "scale";
}

export default function RevealOnScroll({
  children,
  className = "",
  delay = 0,
}: RevealOnScrollProps) {
  const { ref, isVisible } = useScrollReveal(0.05);

  return (
    <div
      ref={ref}
      className={`${className} ${isVisible ? "reveal-visible" : "reveal-hidden"}`}
      style={{
        transitionDelay: isVisible ? `${delay}s` : "0s",
      }}
    >
      {children}
    </div>
  );
}
