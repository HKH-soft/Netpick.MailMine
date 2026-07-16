import React from "react";
import RevealOnScroll from "./ui/RevealOnScroll";
import SectionHeader from "./ui/SectionHeader";
import ResourceCard from "./ui/ResourceCard";

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

export default function ResourceGrid() {
  return (
    <section className="py-24 border-t border-zinc-800/50">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          tag="Resources"
          title="Learn and build"
          subtitle="Tutorials, guides, and documentation to help you get the most out of Netpick."
        />

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {resources.map((resource, i) => (
            <RevealOnScroll key={resource.title} delay={i * 0.1}>
              <ResourceCard
                tag={resource.tag}
                title={resource.title}
                excerpt={resource.excerpt}
                href={resource.href}
                className="h-full"
              />
            </RevealOnScroll>
          ))}
        </div>
      </div>
    </section>
  );
}
