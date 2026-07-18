import { Metadata } from "next";
import RevealOnScroll from "@/components/landing/ui/RevealOnScroll";
import SectionHeader from "@/components/landing/ui/SectionHeader";
import CTABanner from "@/components/landing/ui/CTABanner";

export const metadata: Metadata = {
  title: "Email Scraping Solutions | Netpick",
  description: "Extract email addresses from any source with intelligent scraping.",
};

const challenges = [
  { title: "Manual Collection", description: "Spending hours copying emails from websites and directories." },
  { title: "Data Decay", description: "Contact lists go stale within months without continuous updates." },
  { title: "Low Quality", description: "Scraped emails often contain bounces, traps, and invalid addresses." },
  { title: "Compliance Risk", description: "Navigating GDPR, CAN-SPAM, and regional privacy laws is complex." },
];

const solutions = [
  { title: "Automated Extraction", description: "AI-powered scraping that finds emails across multiple sources simultaneously." },
  { title: "Real-time Verification", description: "Every email is validated before it enters your pipeline." },
  { title: "Quality Scoring", description: "Proprietary scoring system ranks email deliverability from 0-100." },
  { title: "Compliance Built-in", description: "Automatic robots.txt checking, rate limiting, and opt-out management." },
];

const steps = [
  { number: "01", title: "Configure Source", description: "Define your target websites, search queries, or data sources." },
  { number: "02", title: "Set Filters", description: "Apply domain, quality score, and geographic filters." },
  { number: "03", title: "Run Pipeline", description: "Start extraction with automatic proxy rotation and anti-detection." },
  { number: "04", title: "Export Data", description: "Download verified contacts in CSV, JSON, or push to your CRM." },
];

const useCases = [
  { title: "B2B Lead Generation", description: "Build targeted prospect lists for outbound sales campaigns." },
  { title: "Competitor Research", description: "Discover competitor customer bases and market positioning." },
  { title: "Event Attendee Mining", description: "Extract contacts from conference attendee lists and speaker lineups." },
  { title: "Partnership Outreach", description: "Find decision-makers at potential partner companies." },
  { title: "Journalist Discovery", description: "Build media contact lists for PR campaigns." },
  { title: "Academic Research", description: "Gather contact data for studies and surveys." },
];

export default function ScrapingPage() {
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
                Email Scraping
              </h1>
              <p className="text-lg text-[var(--color-text-secondary)] leading-relaxed max-w-2xl">
                Extract verified email addresses from any source with intelligent
                scraping, proxy rotation, and automatic quality scoring.
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
              <SectionHeader tag="The Challenge" title="Manual scraping doesn't scale" align="left" className="mb-8" />
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
              <SectionHeader tag="The Solution" title="Automated, verified extraction" align="left" className="mb-8" />
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
          <SectionHeader tag="How It Works" title="Four steps to verified contacts" subtitle="From configuration to export in minutes." />
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
          <SectionHeader tag="Use Cases" title="Opportunities" subtitle="Discover how teams use email scraping to drive growth." />
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
          title="Start scraping smarter"
          subtitle="Join thousands of teams using Netpick for email extraction."
          primaryAction={{ label: "Get Started Free", href: "/signup" }}
          secondaryAction={{ label: "View Pricing", href: "/#pricing" }}
        />
      </section>
    </div>
  );
}
