"use client";

import React, { useState } from "react";
import RevealOnScroll from "./ui/RevealOnScroll";

const tabs = [
  {
    id: "ownable",
    label: "Ownable",
    title: "Your Data, Your Control",
    description:
      "Every email record you scrape belongs to you. Export, manage, and delete your data at any time with full ownership rights.",
    features: ["Full data export", "Granular access controls", "Audit trail logging"],
  },
  {
    id: "verifiable",
    label: "Verifiable",
    title: "Validated at Every Step",
    description:
      "Multi-layer verification ensures email accuracy. Syntax checks, MX record validation, and disposable email detection happen automatically.",
    features: ["Real-time verification", "MX record validation", "Spam trap detection"],
  },
  {
    id: "scalable",
    label: "Scalable",
    title: "From Startup to Enterprise",
    description:
      "Process thousands of emails per minute with our distributed scraping infrastructure. Scale without compromising on data quality.",
    features: ["Distributed processing", "Rate limit management", "Auto-scaling pipelines"],
  },
];

export default function ValueProps({ className = "" }: { className?: string }) {
  const [activeTab, setActiveTab] = useState(0);

  return (
    <section className={`py-28 ${className}`}>
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <RevealOnScroll>
          <div className="text-center mb-16">
            <h2
              className="text-4xl md:text-5xl lg:text-[56px] font-bold text-white mb-6 leading-tight"
              style={{ fontFamily: "var(--font-heading)" }}
            >
              The economy, rebuilt on
              <br />
              <span style={{ color: "var(--color-accent)" }}>integrity</span>
            </h2>
            <p className="text-[16px] max-w-2xl mx-auto leading-relaxed" style={{ color: "var(--color-text-secondary)" }}>
              Netpick is the only platform where email data is ownable, verifiable,
              and scalable — giving teams full confidence in their outreach.
            </p>
          </div>
        </RevealOnScroll>

        <RevealOnScroll delay={0.1}>
          <div className="flex flex-wrap justify-center gap-2 mb-12">
            {tabs.map((tab, i) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(i)}
                className="px-5 py-2 text-[13px] font-medium rounded-full transition-all duration-300"
                style={{
                  background: activeTab === i ? "rgba(34,197,94,0.1)" : "transparent",
                  border: `1px solid ${activeTab === i ? "rgba(34,197,94,0.2)" : "rgba(255,255,255,0.06)"}`,
                  color: activeTab === i ? "var(--color-accent)" : "var(--color-text-muted)",
                }}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </RevealOnScroll>

        <RevealOnScroll delay={0.2}>
          <div className="glass-card-lg p-8 md:p-12">
            <div className="grid md:grid-cols-2 gap-8 items-center">
              <div>
                <h3 className="text-2xl md:text-3xl font-bold text-white mb-4">
                  {tabs[activeTab].title}
                </h3>
                <p className="leading-relaxed mb-6 text-[15px]" style={{ color: "var(--color-text-secondary)" }}>
                  {tabs[activeTab].description}
                </p>
                <ul className="space-y-3">
                  {tabs[activeTab].features.map((feature) => (
                    <li key={feature} className="flex items-center gap-3 text-white text-[14px]">
                      <div
                        className="w-5 h-5 rounded flex items-center justify-center shrink-0"
                        style={{ background: "rgba(34,197,94,0.12)" }}
                      >
                        <svg className="w-3 h-3" fill="none" stroke="var(--color-accent)" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                        </svg>
                      </div>
                      {feature}
                    </li>
                  ))}
                </ul>
              </div>
              <div
                className="rounded-xl p-6"
                style={{
                  background: "rgba(255,255,255,0.02)",
                  border: "1px solid rgba(255,255,255,0.04)",
                }}
              >
                <div className="space-y-3">
                  <div className="h-3 rounded-full w-3/4" style={{ background: "rgba(255,255,255,0.04)" }} />
                  <div className="h-3 rounded-full w-1/2" style={{ background: "rgba(255,255,255,0.04)" }} />
                  <div className="h-3 rounded-full w-5/6" style={{ background: "rgba(34,197,94,0.08)" }} />
                  <div className="h-3 rounded-full w-2/3" style={{ background: "rgba(255,255,255,0.04)" }} />
                  <div className="h-3 rounded-full w-4/5" style={{ background: "rgba(255,255,255,0.04)" }} />
                </div>
              </div>
            </div>
          </div>
        </RevealOnScroll>
      </div>
    </section>
  );
}
