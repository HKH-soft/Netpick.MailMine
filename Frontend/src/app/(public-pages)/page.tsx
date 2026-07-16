import { Metadata } from "next";
import Hero from "@/components/landing/Hero";
import LogoTicker from "@/components/landing/LogoTicker";
import ValueProps from "@/components/landing/ValueProps";
import FeatureStack from "@/components/landing/FeatureStack";
import IndustryCards from "@/components/landing/IndustryCards";
import Stats from "@/components/landing/Stats";
import GetStarted from "@/components/landing/GetStarted";
import ResourceGrid from "@/components/landing/ResourceGrid";
import CTABanner from "@/components/landing/ui/CTABanner";

export const metadata: Metadata = {
  title: "Netpick — Advanced Email Intelligence Platform",
  description:
    "Extract, analyze, and automate your email data workflows with advanced scraping, real-time analytics, and intelligent pipeline automation.",
};

export default function LandingPage() {
  return (
    <div>
      <Hero />
      <LogoTicker />
      <ValueProps />
      <FeatureStack />
      <IndustryCards />
      <Stats />
      <GetStarted />
      <ResourceGrid />
      <section className="py-24 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <CTABanner
          title="Start building with Netpick today"
          subtitle="Join thousands of teams using Netpick to power their email intelligence workflows."
          primaryAction={{ label: "Get Started Free", href: "/signup" }}
          secondaryAction={{ label: "Contact Sales", href: "/contact" }}
        />
      </section>
    </div>
  );
}
