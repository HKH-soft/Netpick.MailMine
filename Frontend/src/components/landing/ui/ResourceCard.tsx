import React from "react";
import Link from "next/link";

interface ResourceCardProps {
  tag: string;
  title: string;
  excerpt: string;
  href: string;
  className?: string;
}

export default function ResourceCard({
  tag,
  title,
  excerpt,
  href,
  className = "",
}: ResourceCardProps) {
  return (
    <Link
      href={href}
      className={`glass-card block p-6 group ${className}`}
    >
      <span className="inline-block px-3 py-1 text-xs font-medium text-[var(--color-accent)] bg-[var(--color-accent)]/10 rounded-full border border-[var(--color-accent)]/20 mb-4">
        {tag}
      </span>
      <h3 className="text-lg font-semibold text-gradient-white mb-2 group-hover:text-[var(--color-accent)] transition-colors duration-300">
        {title}
      </h3>
      <p className="text-[var(--color-text-secondary)] text-sm leading-relaxed">{excerpt}</p>
    </Link>
  );
}
