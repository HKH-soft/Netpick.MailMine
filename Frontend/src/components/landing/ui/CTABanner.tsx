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
    <section className="relative rounded-3xl border border-zinc-800 bg-gradient-to-br from-brand-500/10 via-zinc-900 to-zinc-900 p-8 md:p-16 text-center overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-brand-500/5 via-transparent to-transparent" />
      <div className="relative">
        <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">
          {title}
        </h2>
        {subtitle && (
          <p className="text-zinc-400 text-lg mb-8 max-w-xl mx-auto">
            {subtitle}
          </p>
        )}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link
            href={primaryAction.href}
            className="inline-flex items-center justify-center px-8 py-3.5 text-sm font-semibold text-black bg-brand-400 rounded-lg hover:bg-brand-300 transition-colors"
          >
            {primaryAction.label}
          </Link>
          {secondaryAction && (
            <Link
              href={secondaryAction.href}
              className="inline-flex items-center justify-center px-8 py-3.5 text-sm font-semibold text-zinc-300 border border-zinc-700 rounded-lg hover:bg-zinc-800 transition-colors"
            >
              {secondaryAction.label}
            </Link>
          )}
        </div>
      </div>
    </section>
  );
}
