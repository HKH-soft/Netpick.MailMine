import React from "react";
import RevealOnScroll from "./ui/RevealOnScroll";
import SectionHeader from "./ui/SectionHeader";
import Link from "next/link";

const industries = [
  {
    title: "Sales Teams",
    description: "Build targeted prospect lists with verified email contacts from any industry or region.",
    href: "/solutions/scraping",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
  {
    title: "Marketing",
    description: "Enrich campaign data with accurate, deliverable email addresses for higher engagement rates.",
    href: "/solutions/analytics",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z" />
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z" />
      </svg>
    ),
  },
  {
    title: "Recruitment",
    description: "Source candidates faster with automated email discovery and verification pipelines.",
    href: "/solutions/automation",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
      </svg>
    ),
  },
  {
    title: "Research",
    description: "Gather contact intelligence for market research, academic studies, and competitive analysis.",
    href: "/solutions/scraping",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
      </svg>
    ),
  },
];

export default function IndustryCards({ className = "" }: { className?: string }) {
  return (
    <section className={`py-28 ${className}`}>
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          tag="Use Cases"
          title="Built for every team"
          subtitle="Whether you're in sales, marketing, or research, Netpick adapts to your workflow."
        />

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-4xl mx-auto">
          {industries.map((industry, i) => (
            <RevealOnScroll key={industry.title} delay={i * 0.08}>
              <Link
                href={industry.href}
                className="glass-card block p-6 h-full group"
              >
                <div className="flex items-start gap-4">
                  <div
                    className="w-10 h-10 rounded-lg flex items-center justify-center shrink-0 transition-all duration-500 group-hover:scale-105"
                    style={{
                      background: "rgba(34,197,94,0.08)",
                      border: "1px solid rgba(34,197,94,0.1)",
                      color: "var(--color-accent)",
                    }}
                  >
                    {industry.icon}
                  </div>
                  <div>
                    <h3 className="text-[16px] font-semibold text-white mb-1">
                      {industry.title}
                    </h3>
                    <p className="text-[13px] leading-relaxed mb-3" style={{ color: "var(--color-text-secondary)" }}>
                      {industry.description}
                    </p>
                    <span className="text-[13px] font-medium inline-flex items-center gap-1 transition-all duration-300 group-hover:gap-2" style={{ color: "var(--color-accent)" }}>
                      Explore
                      <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                      </svg>
                    </span>
                  </div>
                </div>
              </Link>
            </RevealOnScroll>
          ))}
        </div>
      </div>
    </section>
  );
}
