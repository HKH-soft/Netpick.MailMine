import React from "react";

interface SectionHeaderProps {
  tag?: string;
  title: string;
  subtitle?: string;
  align?: "center" | "left";
  className?: string;
}

export default function SectionHeader({
  tag,
  title,
  subtitle,
  align = "center",
  className = "",
}: SectionHeaderProps) {
  return (
    <div
      className={`mb-12 md:mb-16 ${align === "center" ? "text-center" : "text-left"} ${className}`}
    >
      {tag && (
        <span className="inline-block text-sm font-semibold tracking-widest uppercase text-brand-400 mb-4">
          {tag}
        </span>
      )}
      <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-white mb-4">
        {title}
      </h2>
      {subtitle && (
        <p className="text-lg text-zinc-400 max-w-2xl mx-auto">
          {subtitle}
        </p>
      )}
    </div>
  );
}
