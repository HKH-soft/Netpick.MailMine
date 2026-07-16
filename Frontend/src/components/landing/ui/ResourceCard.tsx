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
      className={`group block rounded-2xl border border-zinc-800 bg-zinc-900/50 p-6 transition-all duration-300 hover:border-brand-500/30 hover:bg-zinc-900 ${className}`}
    >
      <span className="inline-block px-2.5 py-1 text-xs font-medium text-brand-400 bg-brand-500/10 rounded-full mb-3">
        {tag}
      </span>
      <h3 className="text-lg font-semibold text-white mb-2 group-hover:text-brand-400 transition-colors">
        {title}
      </h3>
      <p className="text-zinc-400 text-sm leading-relaxed">{excerpt}</p>
    </Link>
  );
}
