import React from "react";
import RevealOnScroll from "./ui/RevealOnScroll";
import StatCounter from "./ui/StatCounter";

const stats = [
  { value: "5000000", label: "Emails Scraped", suffix: "+" },
  { value: "99", label: "Accuracy Rate", suffix: "%" },
  { value: "2500", label: "Active Pipelines", suffix: "+" },
  { value: "150", label: "Integrations", suffix: "+" },
];

export default function Stats() {
  return (
    <section className="py-24 border-t border-zinc-800/50">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <RevealOnScroll>
          <div className="rounded-3xl border border-zinc-800 bg-zinc-900/30 p-8 md:p-16">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-8 md:gap-12">
              {stats.map((stat) => (
                <StatCounter
                  key={stat.label}
                  value={stat.value}
                  label={stat.label}
                  suffix={stat.suffix}
                />
              ))}
            </div>
          </div>
        </RevealOnScroll>
      </div>
    </section>
  );
}
