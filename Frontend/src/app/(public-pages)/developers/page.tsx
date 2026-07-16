import { Metadata } from "next";
import RevealOnScroll from "@/components/landing/ui/RevealOnScroll";
import SectionHeader from "@/components/landing/ui/SectionHeader";
import CodeBlock from "@/components/landing/ui/CodeBlock";
import StatCounter from "@/components/landing/ui/StatCounter";
import CTABanner from "@/components/landing/ui/CTABanner";

export const metadata: Metadata = {
  title: "Developers | Netpick",
  description: "API documentation, SDKs, and integration guides for Netpick.",
};

const apiExample = `curl -X POST https://api.netpick.io/v1/scrape \\
  -H "Authorization: Bearer YOUR_API_KEY" \\
  -H "Content-Type: application/json" \\
  -d '{
    "source": "google_maps",
    "query": "coffee shops in San Francisco",
    "fields": ["email", "phone", "website"]
  }'`;

const responseExample = `{
  "status": "success",
  "results": [
    {
      "email": "info@example.com",
      "phone": "+1-555-0123",
      "website": "https://example.com",
      "verified": true,
      "score": 0.95
    }
  ],
  "total": 147,
  "page": 1
}`;

const stats = [
  { value: "99", label: "Uptime SLA", suffix: "%" },
  { value: "50", label: "Avg Response Time", suffix: "ms" },
  { value: "10000", label: "API Calls / Min", suffix: "+" },
  { value: "150", label: "SDKs & Libraries", suffix: "+" },
];

const integrations = [
  { name: "REST API", description: "Full-featured REST API with comprehensive documentation." },
  { name: "Webhooks", description: "Real-time notifications when scraping jobs complete." },
  { name: "Python SDK", description: "First-class Python support with async/await." },
  { name: "Node.js SDK", description: "TypeScript-first SDK for Node.js applications." },
];

export default function DevelopersPage() {
  return (
    <div>
      {/* Hero */}
      <section className="relative py-24 md:py-32 overflow-hidden">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-brand-500/10 via-black to-black" />
        <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <RevealOnScroll>
            <div className="max-w-3xl">
              <span className="inline-block text-sm font-semibold tracking-widest uppercase text-brand-400 mb-4">
                Developers
              </span>
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold text-white mb-6">
                Build with Netpick
              </h1>
              <p className="text-lg text-zinc-400 leading-relaxed">
                Powerful APIs, comprehensive documentation, and first-class SDKs to
                integrate email intelligence into any application.
              </p>
            </div>
          </RevealOnScroll>
        </div>
      </section>

      {/* Stats */}
      <section className="py-16 border-t border-zinc-800/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            {stats.map((stat) => (
              <StatCounter key={stat.label} value={stat.value} label={stat.label} suffix={stat.suffix} />
            ))}
          </div>
        </div>
      </section>

      {/* Code Preview */}
      <section className="py-24 border-t border-zinc-800/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader
            tag="Quick Start"
            title="Start scraping in minutes"
            subtitle="Make your first API call with just a few lines of code."
          />
          <div className="grid lg:grid-cols-2 gap-6 max-w-5xl mx-auto">
            <RevealOnScroll>
              <CodeBlock code={apiExample} language="bash" filename="terminal" />
            </RevealOnScroll>
            <RevealOnScroll delay={0.1}>
              <CodeBlock code={responseExample} language="json" filename="response.json" />
            </RevealOnScroll>
          </div>
        </div>
      </section>

      {/* Integrations */}
      <section className="py-24 border-t border-zinc-800/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader
            tag="Integrations"
            title="Connect your stack"
            subtitle="Native integrations and SDKs for popular frameworks and languages."
          />
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {integrations.map((item, i) => (
              <RevealOnScroll key={item.name} delay={i * 0.1}>
                <div className="rounded-2xl border border-zinc-800 bg-zinc-900/30 p-6 h-full">
                  <h3 className="text-lg font-semibold text-white mb-2">{item.name}</h3>
                  <p className="text-zinc-400 text-sm leading-relaxed">{item.description}</p>
                </div>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-24 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <CTABanner
          title="Ready to start building?"
          subtitle="Get your API key and start scraping in minutes."
          primaryAction={{ label: "Get API Key", href: "/signup" }}
          secondaryAction={{ label: "Read Documentation", href: "/developers" }}
        />
      </section>
    </div>
  );
}
