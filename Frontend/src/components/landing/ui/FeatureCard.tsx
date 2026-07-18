import React from "react";

interface FeatureCardProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  className?: string;
}

export default function FeatureCard({
  icon,
  title,
  description,
  className = "",
}: FeatureCardProps) {
  return (
    <div className={`glass-card p-6 group ${className}`}>
      <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-[var(--color-accent)]/20 to-[var(--color-accent)]/5 flex items-center justify-center mb-5 text-[var(--color-accent)] border border-[var(--color-accent)]/10 group-hover:border-[var(--color-accent)]/30 group-hover:shadow-[0_0_20px_rgba(41,141,255,0.15)] transition-all duration-400">
        {icon}
      </div>
      <h3 className="text-lg font-semibold text-gradient-white mb-2">{title}</h3>
      <p className="text-[var(--color-text-secondary)] text-sm leading-relaxed">{description}</p>
    </div>
  );
}
