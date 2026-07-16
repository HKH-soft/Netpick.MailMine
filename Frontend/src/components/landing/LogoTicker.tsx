import React from "react";

const logos = [
  "Google",
  "Microsoft",
  "Amazon",
  "Meta",
  "Netflix",
  "Stripe",
  "Shopify",
  "HubSpot",
];

export default function LogoTicker() {
  return (
    <section className="py-16 border-t border-zinc-800/50">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <p className="text-center text-sm text-zinc-500 mb-8 tracking-wide">
          Trusted by leading teams worldwide
        </p>
        <div className="relative overflow-hidden">
          <div className="flex animate-scroll-ticker" style={{ width: "max-content" }}>
            {[...logos, ...logos].map((logo, i) => (
              <div
                key={i}
                className="flex items-center justify-center px-8 md:px-12 shrink-0"
              >
                <span className="text-xl font-bold text-zinc-600 whitespace-nowrap">
                  {logo}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
