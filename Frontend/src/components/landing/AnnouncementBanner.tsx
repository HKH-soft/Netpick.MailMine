"use client";

import React, { useState } from "react";

export default function AnnouncementBanner() {
  const [visible, setVisible] = useState(true);

  if (!visible) return null;

  return (
    <div className="relative overflow-hidden border-b border-white/[0.06]">
      <div
        className="absolute inset-0"
        style={{
          background:
            "linear-gradient(90deg, rgba(41,141,255,0.08) 0%, rgba(122,90,248,0.06) 50%, rgba(41,141,255,0.08) 100%)",
        }}
      />
      <div
        className="absolute inset-0 animate-shimmer"
        style={{
          background:
            "linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.04) 50%, transparent 100%)",
          backgroundSize: "200% 100%",
        }}
      />
      <div
        className="absolute inset-0 backdrop-blur-xl"
        style={{
          background: "rgba(1, 24, 41, 0.6)",
        }}
      />
      <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-2.5 flex items-center justify-center gap-3">
        <span
          className="text-sm font-medium"
          style={{
            background: "linear-gradient(135deg, #d4d4d8 0%, #a1a1aa 100%)",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent",
          }}
        >
          Introducing Netpick v2.0 — Advanced Email Intelligence Platform
        </span>
        <a
          href="/about"
          className="text-sm font-semibold text-gradient-accent transition-all duration-300 hover:opacity-80"
        >
          Learn more →
        </a>
        <button
          onClick={() => setVisible(false)}
          className="absolute right-4 top-1/2 -translate-y-1/2 p-1 rounded-full transition-all duration-300 text-[var(--color-text-muted)] hover:text-white hover:bg-white/[0.08]"
          aria-label="Dismiss banner"
        >
          <svg
            className="w-3.5 h-3.5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>
    </div>
  );
}
