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
                Email Scraping
              </h1>
              <p className="text-lg text-zinc-400 leading-relaxed">
                Extract verified email addresses from any source with intelligent
                scraping, proxy rotation, and automatic quality scoring.
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
              <SectionHeader tag="The Challenge" title="Manual scraping doesn't scale" align="left" className="mb-8" />
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
              <SectionHeader tag="The Solution" title="Automated, verified extraction" align="left" className="mb-8" />
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
          <SectionHeader tag="How It Works" title="Four steps to verified contacts" subtitle="From configuration to export in minutes." />
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
          <SectionHeader tag="Use Cases" title="Opportunities" subtitle="Discover how teams use email scraping to drive growth." />
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
          title="Start scraping smarter"
          subtitle="Join thousands of teams using Netpick for email extraction."
          primaryAction={{ label: "Get Started Free", href: "/signup" }}
          secondaryAction={{ label: "View Pricing", href: "/#pricing" }}
        />
      </section>
    </div>
  );
}
