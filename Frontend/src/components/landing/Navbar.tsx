"use client";

import React, { useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { usePathname } from "next/navigation";
import { useScrolled } from "@/hooks/useScrolled";
import MegaDropdown from "./MegaDropdown";

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
      className={`sticky top-0 z-50 transition-all duration-300 ${
        scrolled
          ? "bg-black/95 backdrop-blur-md border-b border-zinc-800"
          : "bg-transparent"
      }`}
    >
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2.5 shrink-0">
            <Image
              src="/images/Netpick-Platform/Netpick.svg"
              alt="Netpick"
              width={28}
              height={28}
            />
            <span className="text-xl font-bold text-white">Netpick</span>
          </Link>

          {/* Desktop Nav */}
          <nav className="hidden lg:flex items-center gap-1">
            {navLinks.map((link) => (
              <div key={link.name} className="relative group">
                <a
                  href={link.href}
                  className={`px-3 py-2 text-sm font-medium rounded-lg transition-colors ${
                    pathname === link.href || pathname.startsWith(link.href + "/")
                      ? "text-white bg-white/5"
                      : "text-zinc-400 hover:text-white hover:bg-white/5"
                  }`}
                >
                  {link.name}
                  {link.dropdown && (
                    <svg className="inline-block ml-1 w-3 h-3 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  )}
                </a>
                {link.dropdown && (
                  <MegaDropdown categories={link.dropdown.categories} />
                )}
              </div>
            ))}
          </nav>

          {/* CTA */}
          <div className="hidden lg:flex items-center gap-3">
            <Link
              href="/signin"
              className="text-sm font-medium text-zinc-400 hover:text-white transition-colors px-3 py-2"
            >
              Sign In
            </Link>
            <Link
              href="/signup"
              className="text-sm font-semibold text-black bg-brand-400 hover:bg-brand-300 px-5 py-2.5 rounded-lg transition-colors"
            >
              Get Started
            </Link>
          </div>

          {/* Mobile Hamburger */}
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="lg:hidden p-2 text-zinc-400 hover:text-white"
            aria-label="Toggle menu"
          >
            {mobileOpen ? (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            ) : (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            )}
          </button>
        </div>
      </div>

      {/* Mobile Menu */}
      {mobileOpen && (
        <div className="lg:hidden border-t border-zinc-800 bg-black">
          <div className="px-4 py-4 space-y-1">
            {navLinks.map((link) => (
              <a
                key={link.name}
                href={link.href}
                className={`block px-3 py-2.5 text-sm font-medium rounded-lg ${
                  pathname === link.href
                    ? "text-white bg-white/5"
                    : "text-zinc-400 hover:text-white hover:bg-white/5"
                }`}
                onClick={() => setMobileOpen(false)}
              >
                {link.name}
              </a>
            ))}
            <div className="pt-4 border-t border-zinc-800 space-y-2">
              <a
                href="/signin"
                className="block px-3 py-2.5 text-sm font-medium text-zinc-400 hover:text-white"
                onClick={() => setMobileOpen(false)}
              >
                Sign In
              </a>
              <a
                href="/signup"
                className="block px-3 py-2.5 text-sm font-semibold text-center text-black bg-brand-400 rounded-lg"
                onClick={() => setMobileOpen(false)}
              >
                Get Started
              </a>
            </div>
          </div>
        </div>
      )}
    </header>
  );
}
