import { Metadata } from "next";
import RevealOnScroll from "@/components/landing/ui/RevealOnScroll";
import SectionHeader from "@/components/landing/ui/SectionHeader";
import CTABanner from "@/components/landing/ui/CTABanner";

export const metadata: Metadata = {
  title: "About Us | Netpick",
  description: "Learn about Netpick's mission to revolutionize email intelligence.",
};

const principles = [
  {
    number: "01",
    title: "Data Integrity First",
    description: "Every email we process goes through multi-layer verification. We never compromise on data quality.",
  },
  {
    number: "02",
    title: "Ethical Scraping",
    description: "We respect robots.txt, rate limits, and privacy regulations. Our tools are built for compliant data collection.",
  },
  {
    number: "03",
    title: "Transparency",
    description: "Open pricing, clear documentation, and honest communication. No hidden fees or dark patterns.",
  },
  {
    number: "04",
    title: "Continuous Innovation",
    description: "We ship weekly. Our platform evolves with the landscape to stay ahead of anti-scraping measures.",
  },
];

const values = [
  { title: "Privacy", description: "Your data security and privacy are non-negotiable. End-to-end encryption and GDPR compliance." },
  { title: "Excellence", description: "We hold ourselves to the highest standards in engineering, design, and customer support." },
  { title: "Innovation", description: "Continuously pushing the boundaries of what's possible in email intelligence." },
  { title: "Community", description: "Building in the open, contributing to open source, and supporting our users." },
];

const team = [
  { name: "Sarah Johnson", role: "CEO & Founder" },
  { name: "Michael Chen", role: "CTO" },
  { name: "Emily Rodriguez", role: "Head of Product" },
];

export default function AboutPage() {
  return (
    <div>
      <section className="relative py-24 md:py-32 overflow-hidden aurora-bg">
        <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <RevealOnScroll>
            <div className="max-w-3xl glass-card-lg p-10 md:p-14">
              <span className="inline-block text-xs font-semibold tracking-widest uppercase text-gradient-accent mb-4">
                About Netpick
              </span>
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold text-gradient-white mb-6">
                Email intelligence, built with purpose
              </h1>
              <p className="text-lg text-[var(--color-text-secondary)] leading-relaxed">
                Netpick was founded in 2024 with a simple mission: make email data collection
                and analysis accessible, ethical, and powerful for businesses of all sizes.
              </p>
            </div>
          </RevealOnScroll>
        </div>
      </section>

      <div className="section-divider" />

      <section className="py-24">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader
            tag="Principles"
            title="What we stand for"
            subtitle="Four principles guide every decision we make."
          />
          <div className="space-y-4">
            {principles.map((p, i) => (
              <RevealOnScroll key={p.number} delay={i * 0.1}>
                <div className="glass-card p-6 md:p-8 flex flex-col md:flex-row gap-6 items-start">
                  <span className="text-sm font-mono text-gradient-accent shrink-0">{p.number}</span>
                  <div>
                    <h3 className="text-xl font-semibold text-gradient-white mb-2">{p.title}</h3>
                    <p className="text-[var(--color-text-secondary)] leading-relaxed">{p.description}</p>
                  </div>
                </div>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>

      <div className="section-divider" />

      <section className="py-24">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader
            tag="Values"
            title="Our core values"
          />
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {values.map((v, i) => (
              <RevealOnScroll key={v.title} delay={i * 0.1}>
                <div className="glass-card p-6 h-full">
                  <h3 className="text-lg font-semibold text-gradient-white mb-2">{v.title}</h3>
                  <p className="text-[var(--color-text-secondary)] text-sm leading-relaxed">{v.description}</p>
                </div>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>

      <div className="section-divider" />

      <section className="py-24">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader
            tag="Team"
            title="Meet the team"
          />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {team.map((member, i) => (
              <RevealOnScroll key={member.name} delay={i * 0.1}>
                <div className="glass-card p-6 text-center">
                  <div className="w-20 h-20 rounded-full mx-auto mb-4 bg-gradient-to-br from-[var(--color-accent)]/30 to-purple-500/20 border border-[var(--color-glass-border)] flex items-center justify-center">
                    <span className="text-2xl font-bold text-gradient-accent">
                      {member.name.split(" ").map(n => n[0]).join("")}
                    </span>
                  </div>
                  <h4 className="text-lg font-semibold text-gradient-white">{member.name}</h4>
                  <p className="text-[var(--color-text-muted)] text-sm">{member.role}</p>
                </div>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>

      <div className="section-divider" />

      <section className="py-24 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <CTABanner
          title="Join us in building the future"
          subtitle="We're always looking for talented people who share our vision."
          primaryAction={{ label: "View Open Positions", href: "/contact" }}
          secondaryAction={{ label: "Contact Us", href: "/contact" }}
        />
      </section>
    </div>
  );
}
