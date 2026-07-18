import React from "react";
import RevealOnScroll from "./ui/RevealOnScroll";
import SectionHeader from "./ui/SectionHeader";
import Link from "next/link";

const cards = [
  {
    title: "Start Scraping",
    description: "Set up your first email extraction pipeline in minutes.",
    href: "/solutions/scraping",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M13 10V3L4 14h7v7l9-11h-7z" />
      </svg>
    ),
  },
  {
    title: "Analyze Data",
    description: "Gain insights from your email metrics with real-time dashboards.",
    href: "/solutions/analytics",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
    ),
  },
  {
    title: "Automate Workflows",
    description: "Build automated pipelines that run 24/7 without intervention.",
    href: "/solutions/automation",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
      </svg>
    ),
  },
  {
    title: "Join Community",
    description: "Connect with other teams and share best practices.",
    href: "/about",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
      </svg>
    ),
  },
];

export default function GetStarted({ className = "" }: { className?: string }) {
  return (
    <section className={`py-28 ${className}`}>
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          tag="Get Started"
          title="Start connecting"
          subtitle="Join a global community of builders and collaborators."
        />

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-4xl mx-auto">
          {cards.map((card, i) => (
            <RevealOnScroll key={card.title} delay={i * 0.08}>
              <Link
                href={card.href}
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
                    {card.icon}
                  </div>
                  <div>
                    <h3 className="text-[16px] font-semibold text-white mb-1">
                      {card.title}
                    </h3>
                    <p className="text-[13px] leading-relaxed mb-3" style={{ color: "var(--color-text-secondary)" }}>
                      {card.description}
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
