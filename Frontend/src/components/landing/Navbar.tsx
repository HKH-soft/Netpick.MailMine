"use client";

import React, { useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { usePathname } from "next/navigation";
import { useScrolled } from "@/hooks/useScrolled";
import MegaDropdown from "./MegaDropdown";
import LanguageSwitcher from "@/components/common/LanguageSwitcher";

const navLinks = [
  {
    name: "Features",
    href: "/#features",
    dropdown: {
      categories: [
        {
          title: "Platform",
          items: [
            { name: "Email Scraping", href: "/solutions/scraping", description: "Extract contacts from any source" },
            { name: "Analytics", href: "/solutions/analytics", description: "Real-time data insights" },
            { name: "Automation", href: "/solutions/automation", description: "Automated pipeline workflows" },
          ],
        },
        {
          title: "Resources",
          items: [
            { name: "Documentation", href: "/developers", description: "API guides and references" },
            { name: "Blog", href: "/blogs", description: "Latest insights and updates" },
          ],
        },
      ],
    },
  },
  {
    name: "Solutions",
    href: "/solutions/scraping",
    dropdown: {
      categories: [
        {
          title: "Use Cases",
          items: [
            { name: "Sales Teams", href: "/solutions/scraping", description: "Build prospect lists" },
            { name: "Marketing", href: "/solutions/analytics", description: "Campaign data enrichment" },
            { name: "Recruitment", href: "/solutions/automation", description: "Candidate sourcing" },
          ],
        },
        {
          title: "Industries",
          items: [
            { name: "E-commerce", href: "/solutions/scraping", description: "Competitor analysis" },
            { name: "SaaS", href: "/solutions/analytics", description: "Lead generation" },
            { name: "Agencies", href: "/solutions/automation", description: "Client data management" },
          ],
        },
      ],
    },
  },
  { name: "Developers", href: "/developers" },
  { name: "About", href: "/about" },
  { name: "Blog", href: "/blogs" },
];

export default function Navbar() {
  const pathname = usePathname();
  const scrolled = useScrolled(20);
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <header
      className="sticky top-0 z-50 transition-all duration-500"
      style={{
        background: scrolled ? "rgba(0, 0, 0, 0.72)" : "transparent",
        backdropFilter: scrolled ? "blur(20px) saturate(180%)" : "none",
        WebkitBackdropFilter: scrolled ? "blur(20px) saturate(180%)" : "none",
        borderBottom: scrolled ? "1px solid rgba(255, 255, 255, 0.05)" : "1px solid transparent",
      }}
    >
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link href="/" className="flex items-center gap-2.5 shrink-0 group">
            <Image
              src="/images/Netpick-Platform/Netpick.svg"
              alt="Netpick"
              width={28}
              height={28}
              className="transition-all duration-300 group-hover:opacity-80"
            />
            <span className="text-lg font-bold text-white tracking-tight">Netpick</span>
          </Link>

          <nav className="hidden lg:flex items-center gap-0.5">
            {navLinks.map((link) => (
              <div key={link.name} className="relative group">
                <a
                  href={link.href}
                  className="relative px-3 py-2 text-[13px] font-medium rounded-lg transition-all duration-300"
                  style={{
                    color: pathname === link.href || pathname.startsWith(link.href + "/") ? "#ffffff" : "var(--color-text-muted)",
                  }}
                >
                  <span className="relative z-10">{link.name}</span>
                  {link.dropdown && (
                    <svg className="inline-block ml-0.5 w-3 h-3 opacity-40 transition-transform duration-200 group-hover:rotate-180" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  )}
                  {pathname === link.href && (
                    <span className="absolute bottom-0.5 left-3 right-3 h-px" style={{ background: "var(--color-accent)" }} />
                  )}
                </a>
                {link.dropdown && (
                  <MegaDropdown categories={link.dropdown.categories} />
                )}
              </div>
            ))}
          </nav>

          <div className="hidden lg:flex items-center gap-3">
            <LanguageSwitcher />
            <Link
              href="/signin"
              className="text-[13px] font-medium px-4 py-2 rounded-full transition-all duration-300 hover:text-white"
              style={{ color: "var(--color-text-muted)" }}
            >
              Sign In
            </Link>
            <Link
              href="/signup"
              className="btn-gradient text-[13px] px-5 py-2"
            >
              <span>Get started</span>
            </Link>
          </div>

          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="lg:hidden p-2 rounded-lg transition-all duration-300 hover:bg-white/[0.06]"
            style={{ color: "var(--color-text-muted)" }}
            aria-label="Toggle menu"
          >
            {mobileOpen ? (
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            ) : (
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            )}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      <div
        className="lg:hidden overflow-hidden transition-all duration-400"
        style={{
          maxHeight: mobileOpen ? "500px" : "0",
          opacity: mobileOpen ? 1 : 0,
        }}
      >
        <div
          className="mx-4 mb-4 rounded-2xl p-4 space-y-1"
          style={{
            background: "rgba(17, 17, 17, 0.95)",
            backdropFilter: "blur(20px)",
            WebkitBackdropFilter: "blur(20px)",
            border: "1px solid rgba(255,255,255,0.06)",
          }}
        >
          {navLinks.map((link) => (
            <a
              key={link.name}
              href={link.href}
              className="block px-3 py-2.5 text-sm font-medium rounded-xl transition-all duration-300"
              style={{
                color: pathname === link.href ? "#ffffff" : "var(--color-text-muted)",
              }}
              onClick={() => setMobileOpen(false)}
            >
              {link.name}
            </a>
          ))}
          <div className="pt-3 mt-2 space-y-2" style={{ borderTop: "1px solid rgba(255, 255, 255, 0.06)" }}>
            <a href="/signin" className="block px-3 py-2.5 text-sm font-medium rounded-xl transition-all duration-300 hover:text-white" style={{ color: "var(--color-text-muted)" }} onClick={() => setMobileOpen(false)}>
              Sign In
            </a>
            <a href="/signup" className="btn-gradient block text-center text-sm px-5 py-2.5" onClick={() => setMobileOpen(false)}>
              <span>Get started</span>
            </a>
          </div>
        </div>
      </div>
    </header>
  );
}
