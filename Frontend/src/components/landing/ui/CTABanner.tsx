import React from "react";
import Link from "next/link";

interface CTABannerProps {
  title: string;
  subtitle?: string;
  primaryAction: { label: string; href: string };
  secondaryAction?: { label: string; href: string };
}

export default function CTABanner({
  title,
  subtitle,
  primaryAction,
  secondaryAction,
}: CTABannerProps) {
  return (
    <section className="glass-card-lg p-8 md:p-16 text-center relative overflow-hidden">
      <div
        className="absolute inset-0 pointer-events-none"
        style={{
          background: "radial-gradient(ellipse 50% 80% at 50% 100%, rgba(34,197,94,0.06) 0%, transparent 60%)",
        }}
      />
      <div className="relative z-10">
        <h2
          className="text-3xl md:text-4xl font-bold text-white mb-4"
          style={{ fontFamily: "var(--font-heading)" }}
        >
          {title}
        </h2>
        {subtitle && (
          <p className="text-[15px] mb-8 max-w-xl mx-auto" style={{ color: "var(--color-text-secondary)" }}>
            {subtitle}
          </p>
        )}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link
            href={primaryAction.href}
            className="btn-gradient inline-flex items-center justify-center"
          >
            <span>{primaryAction.label}</span>
          </Link>
          {secondaryAction && (
            <Link
              href={secondaryAction.href}
              className="btn-glass inline-flex items-center justify-center"
            >
              {secondaryAction.label}
            </Link>
          )}
        </div>
      </div>
    </section>
  );
}
