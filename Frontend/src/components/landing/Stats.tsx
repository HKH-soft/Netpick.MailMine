import React from "react";
import RevealOnScroll from "./ui/RevealOnScroll";
import StatCounter from "./ui/StatCounter";

const stats = [
  { value: "5000000", label: "Emails Scraped", suffix: "+" },
  { value: "99", label: "Accuracy Rate", suffix: "%" },
  { value: "2500", label: "Active Pipelines", suffix: "+" },
  { value: "150", label: "Integrations", suffix: "+" },
];

export default function Stats({ className = "" }: { className?: string }) {
  return (
    <section className={`py-28 ${className}`}>
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <RevealOnScroll>
          <div className="glass-card-lg p-8 md:p-16">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-8 md:gap-12">
              {stats.map((stat, i) => (
                <div key={stat.label} className="relative">
                  {i > 0 && (
                    <div
                      className="hidden md:block absolute left-0 top-1/2 -translate-y-1/2 -translate-x-6 w-px h-10"
                      style={{ background: "rgba(255,255,255,0.04)" }}
                    />
                  )}
                  <StatCounter
                    value={stat.value}
                    label={stat.label}
                    suffix={stat.suffix}
                  />
                </div>
              ))}
            </div>
          </div>
        </RevealOnScroll>
      </div>
    </section>
  );
}
