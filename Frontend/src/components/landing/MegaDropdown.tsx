"use client";

import React from "react";

interface MegaDropdownItem {
  name: string;
  href: string;
  description?: string;
}

interface MegaDropdownCategory {
  title: string;
  items: MegaDropdownItem[];
}

interface MegaDropdownProps {
  categories: MegaDropdownCategory[];
}

export default function MegaDropdown({ categories }: MegaDropdownProps) {
  return (
    <div
      className="absolute top-full left-1/2 -translate-x-1/2 pt-4 w-screen max-w-4xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-500 ease-out"
      style={{ transitionTimingFunction: "cubic-bezier(0.22, 1, 0.36, 1)" }}
    >
      <div
        className="rounded-2xl p-6"
        style={{
          background: "rgba(1, 24, 41, 0.85)",
          backdropFilter: "blur(24px) saturate(180%)",
          WebkitBackdropFilter: "blur(24px) saturate(180%)",
          border: "1px solid rgba(255, 255, 255, 0.08)",
          boxShadow:
            "0 24px 80px -12px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255, 255, 255, 0.03) inset",
        }}
      >
        <div
          className={`grid gap-6 ${categories.length <= 2 ? "grid-cols-2" : "grid-cols-3"}`}
        >
          {categories.map((category) => (
            <div key={category.title}>
              <h4
                className="text-xs font-semibold tracking-widest uppercase mb-4 text-gradient-accent"
              >
                {category.title}
              </h4>
              <ul className="space-y-1">
                {category.items.map((item) => (
                  <li key={item.name}>
                    <a
                      href={item.href}
                      className="block rounded-xl px-3 py-2.5 transition-all duration-400 ease-out hover:bg-white/[0.06] hover:text-white"
                      style={{
                        color: "var(--color-text-secondary)",
                      }}
                    >
                      <span className="font-medium text-sm">{item.name}</span>
                      {item.description && (
                        <span
                          className="block text-xs mt-0.5"
                          style={{ color: "var(--color-text-muted)" }}
                        >
                          {item.description}
                        </span>
                      )}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
