import { Metadata } from "next";
import RevealOnScroll from "@/components/landing/ui/RevealOnScroll";
import SectionHeader from "@/components/landing/ui/SectionHeader";
import CTABanner from "@/components/landing/ui/CTABanner";

export const metadata: Metadata = {
  title: "Email Analytics Solutions | Netpick",
  description: "Real-time analytics and insights for your email data pipelines.",
};

const challenges = [
  { title: "Blind Operations", description: "No visibility into which emails perform and which bounce." },
  { title: "Fragmented Data", description: "Metrics scattered across multiple tools and spreadsheets." },
  { title: "Slow Reporting", description: "Manual report generation takes hours instead of seconds." },
  { title: "No Benchmarking", description: "Unable to compare performance across campaigns or time periods." },
];

const solutions = [
  { title: "Unified Dashboard", description: "All your email metrics in one real-time, interactive dashboard." },
  { title: "Smart Alerts", description: "Automatic notifications when deliverability drops or anomalies are detected." },
  { title: "Automated Reports", description: "Schedule and send performance reports to stakeholders automatically." },
  { title: "Predictive Analytics", description: "ML-powered predictions for campaign performance and optimal send times." },
];

const steps = [
  { number: "01", title: "Connect Sources", description: "Link your email providers, CRMs, and data pipelines." },
  { number: "02", title: "Configure Metrics", description: "Choose which KPIs matter most to your team." },
  { number: "03", title: "Monitor Real-time", description: "Watch your dashboards update as data flows in." },
  { number: "04", title: "Act on Insights", description: "Use recommendations to optimize your email strategy." },
];

const useCases = [
  { title: "Campaign Optimization", description: "A/B test subject lines, send times, and content for maximum engagement." },
  { title: "Deliverability Monitoring", description: "Track inbox placement rates and sender reputation in real-time." },
  { title: "Team Performance", description: "Measure individual and team scraping pipeline performance." },
  { title: "ROI Tracking", description: "Attribute revenue and conversions to specific email campaigns." },
  { title: "Compliance Reporting", description: "Generate audit-ready reports for GDPR and CAN-SPAM compliance." },
  { title: "Competitive Benchmarking", description: "Compare your metrics against industry averages and competitors." },
];

export default function AnalyticsPage() {
  return (
    <div>
      {/* Hero */}
      <section className="relative py-24 md:py-32 overflow-hidden">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-brand-500/10 via-black to-black" />
        <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <RevealOnScroll>
            <div className="max-w-3xl">
              <span className="inline-block text-sm font-semibold tracking-widest uppercase text-brand-400 mb-4">
                Solutions
              </span>
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold text-white mb-6">
                Email Analytics
              </h1>
              <p className="text-lg text-zinc-400 leading-relaxed">
                Real-time dashboards, smart alerts, and predictive analytics to
                optimize every aspect of your email operations.
              </p>
            </div>
          </RevealOnScroll>
        </div>
      </section>

      {/* Challenge vs Solution */}
      <section className="py-24 border-t border-zinc-800/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="grid lg:grid-cols-2 gap-12">
            <div>
              <SectionHeader tag="The Challenge" title="Flying blind on email data" align="left" className="mb-8" />
              <div className="space-y-4">
                {challenges.map((c, i) => (
                  <RevealOnScroll key={c.title} delay={i * 0.1}>
                    <div className="rounded-xl border border-zinc-800 bg-zinc-900/30 p-5">
                      <h3 className="font-semibold text-white mb-1">{c.title}</h3>
                      <p className="text-sm text-zinc-400">{c.description}</p>
                    </div>
                  </RevealOnScroll>
                ))}
              </div>
            </div>
            <div>
              <SectionHeader tag="The Solution" title="Intelligence at your fingertips" align="left" className="mb-8" />
              <div className="space-y-4">
                {solutions.map((s, i) => (
                  <RevealOnScroll key={s.title} delay={i * 0.1}>
                    <div className="rounded-xl border border-brand-500/20 bg-brand-500/5 p-5">
                      <h3 className="font-semibold text-white mb-1">{s.title}</h3>
                      <p className="text-sm text-zinc-400">{s.description}</p>
                    </div>
                  </RevealOnScroll>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-24 border-t border-zinc-800/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader tag="How It Works" title="From data to decisions" subtitle="Set up analytics in four simple steps." />
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {steps.map((step, i) => (
              <RevealOnScroll key={step.number} delay={i * 0.1}>
                <div className="rounded-2xl border border-zinc-800 bg-zinc-900/30 p-6 h-full">
                  <span className="text-sm font-mono text-zinc-600">{step.number}</span>
                  <h3 className="text-lg font-semibold text-white mt-2 mb-2">{step.title}</h3>
                  <p className="text-zinc-400 text-sm leading-relaxed">{step.description}</p>
                </div>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>

      {/* Use Cases */}
      <section className="py-24 border-t border-zinc-800/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader tag="Use Cases" title="Opportunities" subtitle="Unlock insights across your entire email operation." />
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {useCases.map((uc, i) => (
              <RevealOnScroll key={uc.title} delay={i * 0.1}>
                <div className="rounded-2xl border border-zinc-800 bg-zinc-900/30 p-6 h-full">
                  <h3 className="text-lg font-semibold text-white mb-2">{uc.title}</h3>
                  <p className="text-zinc-400 text-sm leading-relaxed">{uc.description}</p>
                </div>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-24 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <CTABanner
          title="See your data in a new light"
          subtitle="Get actionable insights from day one with Netpick Analytics."
          primaryAction={{ label: "Start Free Trial", href: "/signup" }}
          secondaryAction={{ label: "Book a Demo", href: "/contact" }}
        />
      </section>
    </div>
  );
}
