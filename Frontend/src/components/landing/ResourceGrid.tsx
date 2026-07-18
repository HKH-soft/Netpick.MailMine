import React from "react";
import RevealOnScroll from "./ui/RevealOnScroll";
import SectionHeader from "./ui/SectionHeader";
import Link from "next/link";

const resources = [
  {
    tag: "Tutorial",
    title: "Getting Started with Email Scraping",
    excerpt: "A step-by-step guide to setting up your first scraping pipeline with Netpick.",
    href: "/blogs/1",
  },
  {
    tag: "Guide",
    title: "Email Validation Best Practices",
    excerpt: "Learn how to ensure your scraped email data is accurate and deliverable.",
    href: "/blogs/2",
  },
  {
    tag: "Case Study",
    title: "How TeamX 3x'd Their Outreach",
    excerpt: "See how a sales team used Netpick to triple their response rates.",
    href: "/blogs/3",
  },
  {
    tag: "Documentation",
    title: "API Reference",
    excerpt: "Complete API documentation with examples for all Netpick endpoints.",
    href: "/developers",
  },
];

export default function ResourceGrid({ className = "" }: { className?: string }) {
  return (
    <section className={`py-28 ${className}`}>
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          tag="Resources"
          title="Stay in the loop"
          subtitle="Tutorials, guides, and documentation to help you get the most out of Netpick."
        />

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {resources.map((resource, i) => (
            <RevealOnScroll key={resource.title} delay={i * 0.08}>
              <Link
                href={resource.href}
                className="glass-card block p-6 h-full group"
              >
                <span
                  className="inline-block px-2.5 py-1 text-[11px] font-medium rounded-md mb-3"
                  style={{
                    background: "rgba(34,197,94,0.08)",
                    color: "var(--color-accent)",
                  }}
                >
                  {resource.tag}
                </span>
                <h3 className="text-[15px] font-semibold mb-2 text-white leading-snug">
                  {resource.title}
                </h3>
                <p className="text-[13px] leading-relaxed" style={{ color: "var(--color-text-secondary)" }}>
                  {resource.excerpt}
                </p>
              </Link>
            </RevealOnScroll>
          ))}
        </div>
      </div>
    </section>
  );
}
