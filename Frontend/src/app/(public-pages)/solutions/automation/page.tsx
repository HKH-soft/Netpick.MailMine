import { Metadata } from "next";
import RevealOnScroll from "@/components/landing/ui/RevealOnScroll";
import SectionHeader from "@/components/landing/ui/SectionHeader";
import CTABanner from "@/components/landing/ui/CTABanner";

export const metadata: Metadata = {
  title: "Email Automation Solutions | Netpick",
  description: "Automate your email data workflows with intelligent pipelines.",
};

const challenges = [
  { title: "Repetitive Tasks", description: "Manually cleaning, enriching, and routing email data every day." },
  { title: "Human Error", description: "Manual processes introduce mistakes that compound over time." },
  { title: "Scaling Limits", description: "Team size becomes the bottleneck for data processing volume." },
  { title: "Tool Sprawl", description: "Multiple disconnected tools for different stages of the workflow." },
];

const solutions = [
  { title: "Visual Pipelines", description: "Drag-and-drop pipeline builder with conditional logic and branching." },
  { title: "Auto-enrichment", description: "Automatically append company data, social profiles, and verification scores." },
  { title: "Smart Routing", description: "Route leads to the right team member based on score, region, or custom rules." },
  { title: "CRM Sync", description: "Bi-directional sync with Salesforce, HubSpot, Pipedrive, and more." },
];

const steps = [
  { number: "01", title: "Design Pipeline", description: "Use the visual editor to map your ideal data workflow." },
  { number: "02", title: "Set Triggers", description: "Define what starts the pipeline — new data, schedule, or webhook." },
  { number: "03", title: "Configure Actions", description: "Add scraping, validation, enrichment, and routing steps." },
  { number: "04", title: "Monitor & Optimize", description: "Track pipeline performance and refine as needed." },
];

const useCases = [
  { title: "Lead Scoring", description: "Automatically score and qualify incoming leads based on custom criteria." },
  { title: "List Hygiene", description: "Keep your email lists clean with scheduled verification and removal." },
  { title: "Onboarding Sequences", description: "Trigger welcome emails and follow-ups based on user actions." },
  { title: "Data Enrichment", description: "Append missing fields like job title, company size, and social links." },
  { title: "Multi-channel Outreach", description: "Coordinate email, LinkedIn, and phone outreach from one pipeline." },
  { title: "Compliance Automation", description: "Automatically honor opt-out requests and maintain do-not-contact lists." },
];

export default function AutomationPage() {
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
                Email Automation
              </h1>
              <p className="text-lg text-[var(--color-text-secondary)] leading-relaxed max-w-2xl">
                Visual pipeline builder, smart routing, and CRM sync to automate
                your entire email data workflow from start to finish.
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
              <SectionHeader tag="The Challenge" title="Manual workflows don't scale" align="left" className="mb-8" />
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
              <SectionHeader tag="The Solution" title="Workflows that run themselves" align="left" className="mb-8" />
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
          <SectionHeader tag="How It Works" title="Automate in four steps" subtitle="From design to production in minutes." />
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
          <SectionHeader tag="Use Cases" title="Opportunities" subtitle="Automate any email workflow imaginable." />
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
          title="Automate your email workflows"
          subtitle="Stop doing repetitive tasks. Let Netpick handle the heavy lifting."
          primaryAction={{ label: "Get Started Free", href: "/signup" }}
          secondaryAction={{ label: "Book a Demo", href: "/contact" }}
        />
      </section>
    </div>
  );
}
