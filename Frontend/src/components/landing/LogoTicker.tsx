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
    <section className="py-16">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <p className="text-center text-[13px] mb-10 tracking-wide font-medium" style={{ color: "var(--color-text-muted)" }}>
          The most innovative companies build on Netpick
        </p>
        <div className="relative overflow-hidden py-2">
          <div
            className="absolute left-0 top-0 bottom-0 w-32 z-10 pointer-events-none"
            style={{ background: "linear-gradient(90deg, #000 0%, transparent 100%)" }}
          />
          <div
            className="absolute right-0 top-0 bottom-0 w-32 z-10 pointer-events-none"
            style={{ background: "linear-gradient(270deg, #000 0%, transparent 100%)" }}
          />
          <div className="flex animate-scroll-ticker" style={{ width: "max-content" }}>
            {[...logos, ...logos].map((logo, i) => (
              <div
                key={i}
                className="flex items-center justify-center px-8 md:px-12 shrink-0"
              >
                <span
                  className="text-lg font-semibold whitespace-nowrap"
                  style={{ color: "rgba(255,255,255,0.2)" }}
                >
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
