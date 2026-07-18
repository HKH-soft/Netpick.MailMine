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
      <section className="relative py-24 md:py-32 overflow-hidden aurora-bg noise-overlay">
        <div className="absolute inset-0 bg-gradient-to-b from-transparent via-[var(--color-bg-base)]/80 to-[var(--color-bg-base)]" />
        <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <RevealOnScroll>
            <div className="max-w-3xl">
              <span className="inline-block text-xs font-semibold tracking-[0.2em] uppercase text-[var(--color-accent-light)] mb-6 px-4 py-2 rounded-full border border-[var(--color-accent)]/20 bg-[var(--color-accent)]/5 backdrop-blur-sm">
                Solutions
              </span>
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold text-gradient-white mb-6" style={{ fontFamily: "var(--font-heading)" }}>
                Email Analytics
              </h1>
              <p className="text-lg text-[var(--color-text-secondary)] leading-relaxed max-w-2xl">
                Real-time dashboards, smart alerts, and predictive analytics to
                optimize every aspect of your email operations.
              </p>
            </div>
          </RevealOnScroll>
        </div>
      </section>

      <div className="section-divider mx-auto max-w-7xl" />

      <section className="py-24">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="grid lg:grid-cols-2 gap-12">
            <div>
              <SectionHeader tag="The Challenge" title="Flying blind on email data" align="left" className="mb-8" />
              <div className="space-y-4">
                {challenges.map((c, i) => (
                  <RevealOnScroll key={c.title} delay={i * 0.1}>
                    <div className="glass-card p-5 border-[rgba(240,68,56,0.08)] bg-[rgba(240,68,56,0.03)] hover:border-[rgba(240,68,56,0.15)] hover:bg-[rgba(240,68,56,0.05)]">
                      <h3 className="font-semibold text-[var(--color-text-primary)] mb-1">{c.title}</h3>
                      <p className="text-sm text-[var(--color-text-secondary)]">{c.description}</p>
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
                    <div className="glass-card p-5 border-[rgba(41,141,255,0.12)] bg-[rgba(41,141,255,0.04)] hover:border-[rgba(41,141,255,0.25)] hover:bg-[rgba(41,141,255,0.07)] glow-accent">
                      <h3 className="font-semibold text-[var(--color-text-primary)] mb-1">{s.title}</h3>
                      <p className="text-sm text-[var(--color-text-secondary)]">{s.description}</p>
                    </div>
                  </RevealOnScroll>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="section-divider mx-auto max-w-7xl" />

      <section className="py-24">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader tag="How It Works" title="From data to decisions" subtitle="Set up analytics in four simple steps." />
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mt-12">
            {steps.map((step, i) => (
              <RevealOnScroll key={step.number} delay={i * 0.1}>
                <div className="glass-card p-6 h-full relative group">
                  <span className="text-3xl font-bold text-gradient-accent block mb-3" style={{ fontFamily: "var(--font-heading)" }}>{step.number}</span>
                  <h3 className="text-lg font-semibold text-[var(--color-text-primary)] mb-2">{step.title}</h3>
                  <p className="text-[var(--color-text-secondary)] text-sm leading-relaxed">{step.description}</p>
                  {i < steps.length - 1 && (
                    <div className="hidden lg:block absolute top-1/2 -right-2 w-4 h-[1px] bg-gradient-to-r from-[var(--color-accent)]/30 to-transparent" />
                  )}
                </div>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>

      <div className="section-divider mx-auto max-w-7xl" />

      <section className="py-24">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader tag="Use Cases" title="Opportunities" subtitle="Unlock insights across your entire email operation." />
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-12">
            {useCases.map((uc, i) => (
              <RevealOnScroll key={uc.title} delay={i * 0.1}>
                <div className="glass-card p-6 h-full">
                  <h3 className="text-lg font-semibold text-[var(--color-text-primary)] mb-2">{uc.title}</h3>
                  <p className="text-[var(--color-text-secondary)] text-sm leading-relaxed">{uc.description}</p>
                </div>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>

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
