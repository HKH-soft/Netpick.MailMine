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
      className={`mb-14 md:mb-18 ${align === "center" ? "text-center" : "text-left"} ${className}`}
    >
      {tag && (
        <span className="inline-flex items-center gap-2 text-[11px] font-semibold tracking-[0.15em] uppercase mb-5" style={{ color: "var(--color-accent)" }}>
          <span className="w-1.5 h-1.5 rounded-full" style={{ background: "var(--color-accent)" }} />
          {tag}
        </span>
      )}
      <h2
        className="text-3xl md:text-4xl lg:text-[44px] font-bold text-white mb-4 leading-tight"
        style={{ fontFamily: "var(--font-heading)" }}
      >
        {title}
      </h2>
      {subtitle && (
        <p className="text-[16px] max-w-2xl mx-auto leading-relaxed" style={{ color: "var(--color-text-secondary)" }}>
          {subtitle}
        </p>
      )}
    </div>
  );
}
