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
      {/* Hero */}
      <section className="relative py-24 md:py-32 overflow-hidden">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-brand-500/10 via-black to-black" />
        <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <RevealOnScroll>
            <div className="max-w-3xl">
              <span className="inline-block text-sm font-semibold tracking-widest uppercase text-brand-400 mb-4">
                About Netpick
              </span>
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold text-white mb-6">
                Email intelligence, built with purpose
              </h1>
              <p className="text-lg text-zinc-400 leading-relaxed">
                Netpick was founded in 2024 with a simple mission: make email data collection
                and analysis accessible, ethical, and powerful for businesses of all sizes.
              </p>
            </div>
          </RevealOnScroll>
        </div>
      </section>

      {/* Principles */}
      <section className="py-24 border-t border-zinc-800/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader
            tag="Principles"
            title="What we stand for"
            subtitle="Four principles guide every decision we make."
          />
          <div className="space-y-4">
            {principles.map((p, i) => (
              <RevealOnScroll key={p.number} delay={i * 0.1}>
                <div className="rounded-2xl border border-zinc-800 bg-zinc-900/30 p-6 md:p-8 flex flex-col md:flex-row gap-6 items-start">
                  <span className="text-sm font-mono text-zinc-600 shrink-0">{p.number}</span>
                  <div>
                    <h3 className="text-xl font-semibold text-white mb-2">{p.title}</h3>
                    <p className="text-zinc-400 leading-relaxed">{p.description}</p>
                  </div>
                </div>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>

      {/* Values */}
      <section className="py-24 border-t border-zinc-800/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader
            tag="Values"
            title="Our core values"
          />
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {values.map((v, i) => (
              <RevealOnScroll key={v.title} delay={i * 0.1}>
                <div className="rounded-2xl border border-zinc-800 bg-zinc-900/30 p-6 h-full">
                  <h3 className="text-lg font-semibold text-white mb-2">{v.title}</h3>
                  <p className="text-zinc-400 text-sm leading-relaxed">{v.description}</p>
                </div>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>

      {/* Team */}
      <section className="py-24 border-t border-zinc-800/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeader
            tag="Team"
            title="Meet the team"
          />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {team.map((member, i) => (
              <RevealOnScroll key={member.name} delay={i * 0.1}>
                <div className="rounded-2xl border border-zinc-800 bg-zinc-900/30 p-6 text-center">
                  <div className="w-20 h-20 bg-zinc-800 rounded-full mx-auto mb-4" />
                  <h4 className="text-lg font-semibold text-white">{member.name}</h4>
                  <p className="text-zinc-500 text-sm">{member.role}</p>
                </div>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
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
