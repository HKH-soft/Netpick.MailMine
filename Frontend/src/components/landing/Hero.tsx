"use client";

import React from "react";
import Link from "next/link";
import RevealOnScroll from "./ui/RevealOnScroll";

export default function Hero() {
  return (
    <section className="relative overflow-hidden">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-brand-500/10 via-black to-black" />

      <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-24 md:py-32 lg:py-40">
        <RevealOnScroll>
          <div className="text-center max-w-4xl mx-auto">
            <span className="inline-block text-sm font-semibold tracking-widest uppercase text-brand-400 mb-6">
              Email Intelligence Platform
            </span>
            <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold text-white mb-6 leading-tight">
              Unlock the Power of{" "}
              <span className="text-brand-400">Email Intelligence</span>
            </h1>
            <p className="text-lg md:text-xl text-zinc-400 max-w-2xl mx-auto mb-10 leading-relaxed">
              Extract, analyze, and automate your email data workflows with
              advanced scraping, real-time analytics, and intelligent pipeline
              automation.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link
                href="/signup"
                className="inline-flex items-center justify-center px-8 py-4 text-base font-semibold text-black bg-brand-400 rounded-xl hover:bg-brand-300 transition-colors"
              >
                Get Started Free
                <svg className="ml-2 w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </Link>
              <Link
                href="/developers"
                className="inline-flex items-center justify-center px-8 py-4 text-base font-semibold text-zinc-300 border border-zinc-700 rounded-xl hover:bg-zinc-800 transition-colors"
              >
                View Documentation
              </Link>
            </div>
          </div>
        </RevealOnScroll>

        {/* Decorative grid */}
        <div className="absolute inset-0 -z-10 bg-[linear-gradient(to_right,#18181b_1px,transparent_1px),linear-gradient(to_bottom,#18181b_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)] opacity-20" />
      </div>
    </section>
  );
}
