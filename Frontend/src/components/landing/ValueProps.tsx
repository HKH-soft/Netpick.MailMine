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

export default function ValueProps() {
  const [activeTab, setActiveTab] = useState(0);

  return (
    <section className="py-24">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <RevealOnScroll>
          <div className="text-center mb-16">
            <span className="inline-block text-sm font-semibold tracking-widest uppercase text-brand-400 mb-4">
              Why Netpick
            </span>
            <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-white mb-4">
              Email data, rebuilt for business
            </h2>
            <p className="text-lg text-zinc-400 max-w-2xl mx-auto">
              A new standard for how teams collect, verify, and leverage email intelligence.
            </p>
          </div>
        </RevealOnScroll>

        <RevealOnScroll delay={0.1}>
          <div className="flex flex-wrap justify-center gap-2 mb-12">
            {tabs.map((tab, i) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(i)}
                className={`px-5 py-2.5 text-sm font-medium rounded-lg transition-colors ${
                  activeTab === i
                    ? "bg-brand-500/10 text-brand-400 border border-brand-500/30"
                    : "text-zinc-400 hover:text-white border border-transparent hover:border-zinc-800"
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </RevealOnScroll>

        <RevealOnScroll delay={0.2}>
          <div className="rounded-2xl border border-zinc-800 bg-zinc-900/50 p-8 md:p-12">
            <div className="grid md:grid-cols-2 gap-8 items-center">
              <div>
                <h3 className="text-2xl md:text-3xl font-bold text-white mb-4">
                  {tabs[activeTab].title}
                </h3>
                <p className="text-zinc-400 leading-relaxed mb-6">
                  {tabs[activeTab].description}
                </p>
                <ul className="space-y-3">
                  {tabs[activeTab].features.map((feature) => (
                    <li key={feature} className="flex items-center gap-3 text-zinc-300">
                      <svg className="w-5 h-5 text-brand-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                      {feature}
                    </li>
                  ))}
                </ul>
              </div>
              <div className="rounded-xl bg-zinc-950 border border-zinc-800 p-6">
                <div className="space-y-4">
                  <div className="h-4 bg-zinc-800 rounded w-3/4" />
                  <div className="h-4 bg-zinc-800 rounded w-1/2" />
                  <div className="h-4 bg-brand-500/20 rounded w-5/6" />
                  <div className="h-4 bg-zinc-800 rounded w-2/3" />
                  <div className="h-4 bg-zinc-800 rounded w-4/5" />
                </div>
              </div>
            </div>
          </div>
        </RevealOnScroll>
      </div>
    </section>
  );
}
