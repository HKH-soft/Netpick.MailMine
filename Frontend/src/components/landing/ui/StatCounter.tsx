"use client";

import React, { useEffect, useState } from "react";
import { useScrollReveal } from "@/hooks/useScrollReveal";

interface StatCounterProps {
  value: string;
  label: string;
  suffix?: string;
}

export default function StatCounter({ value, label, suffix = "" }: StatCounterProps) {
  const { ref, isVisible } = useScrollReveal(0.3);
  const [displayed, setDisplayed] = useState("0");

  useEffect(() => {
    if (!isVisible) return;
    const numericPart = value.replace(/[^0-9]/g, "");
    const target = parseInt(numericPart, 10);
    if (isNaN(target)) {
      setDisplayed(value);
      return;
    }

    const duration = 1500;
    const steps = 40;
    const increment = target / steps;
    let current = 0;
    let step = 0;

    const timer = setInterval(() => {
      step++;
      current = Math.min(Math.round(increment * step), target);
      const prefix = value.replace(/[0-9]/g, "").includes("+") ? "" : "";
      setDisplayed(`${prefix}${current.toLocaleString()}`);
      if (step >= steps) {
        setDisplayed(`${prefix}${target.toLocaleString()}`);
        clearInterval(timer);
      }
    }, duration / steps);

    return () => clearInterval(timer);
  }, [isVisible, value]);

  return (
    <div ref={ref} className={`text-center ${isVisible ? "glow-accent" : ""} rounded-2xl p-6 transition-all duration-700`} style={{ transitionTimingFunction: "cubic-bezier(0.16, 1, 0.3, 1)" }}>
      <div className="text-4xl md:text-5xl font-bold text-gradient-accent mb-3 font-mono">
        {displayed}{suffix}
      </div>
      <div className="text-[var(--color-text-muted)] text-sm tracking-wide uppercase">{label}</div>
    </div>
  );
}
