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
    <div className="absolute top-full left-1/2 -translate-x-1/2 pt-4 w-screen max-w-4xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
      <div className="rounded-2xl border border-zinc-800 bg-zinc-900 p-6 shadow-2xl shadow-black/50">
        <div className={`grid gap-6 ${categories.length <= 2 ? "grid-cols-2" : "grid-cols-3"}`}>
          {categories.map((category) => (
            <div key={category.title}>
              <h4 className="text-xs font-semibold tracking-widest uppercase text-zinc-500 mb-3">
                {category.title}
              </h4>
              <ul className="space-y-1">
                {category.items.map((item) => (
                  <li key={item.name}>
                    <a
                      href={item.href}
                      className="block rounded-lg px-3 py-2 text-sm text-zinc-300 hover:bg-zinc-800 hover:text-white transition-colors"
                    >
                      <span className="font-medium">{item.name}</span>
                      {item.description && (
                        <span className="block text-xs text-zinc-500 mt-0.5">
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
