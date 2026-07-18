import React from "react";
import RevealOnScroll from "./ui/RevealOnScroll";
import SectionHeader from "./ui/SectionHeader";

const features = [
  {
    number: "01",
    title: "Email Scraping Engine",
    description:
      "Intelligent scraping with proxy rotation, anti-detection, and multi-source extraction from websites, social media, and public databases.",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
      </svg>
    ),
  },
  {
    number: "02",
    title: "Parsing & Validation",
    description:
      "Real-time email verification with syntax checks, MX record validation, disposable email detection, and spam trap identification.",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
      </svg>
    ),
  },
  {
    number: "03",
    title: "Analytics Dashboard",
    description:
      "Real-time metrics, campaign performance tracking, deliverability scores, and comprehensive reporting across all your email pipelines.",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
    ),
  },
  {
    number: "04",
    title: "Pipeline Automation",
    description:
      "Automated workflows that process, enrich, and route email data. Set triggers, conditions, and actions without writing code.",
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M13 10V3L4 14h7v7l9-11h-7z" />
      </svg>
    ),
  },
];

export default function FeatureStack({ className = "" }: { className?: string }) {
  return (
    <section className={`py-28 ${className}`}>
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          tag="Platform"
          title="Innovation, engineered"
          subtitle="Four powerful modules working together to transform your email data operations."
        />

        <div className="max-w-3xl mx-auto">
          {features.map((feature, i) => (
            <RevealOnScroll key={feature.number} delay={i * 0.08}>
              <div
                className="flex gap-6 items-start py-8 group"
                style={{ borderBottom: i < features.length - 1 ? "1px solid rgba(255,255,255,0.04)" : "none" }}
              >
                <div className="flex items-center gap-4 shrink-0">
                  <span
                    className="text-[13px] font-mono font-semibold"
                    style={{ color: "var(--color-accent)", minWidth: "28px" }}
                  >
                    {feature.number}
                  </span>
                  <div
                    className="w-10 h-10 rounded-lg flex items-center justify-center shrink-0 transition-all duration-500 group-hover:scale-105"
                    style={{
                      background: "rgba(34,197,94,0.08)",
                      border: "1px solid rgba(34,197,94,0.1)",
                      color: "var(--color-accent)",
                    }}
                  >
                    {feature.icon}
                  </div>
                </div>
                <div>
                  <h3 className="text-[17px] font-semibold text-white mb-2">
                    {feature.title}
                  </h3>
                  <p className="leading-relaxed text-[14px]" style={{ color: "var(--color-text-secondary)" }}>
                    {feature.description}
                  </p>
                </div>
              </div>
            </RevealOnScroll>
          ))}
        </div>
      </div>
    </section>
  );
}
