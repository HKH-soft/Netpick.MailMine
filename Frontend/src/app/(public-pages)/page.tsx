import { Metadata } from "next";
import React from "react";
import Link from "next/link";

export const metadata: Metadata = {
  title: "Netpick - Advanced Email Scraping Platform",
  description: "Powerful email scraping and analytics platform for businesses",
};

export default function LandingPage() {
  return (
    <div className="space-y-16">
      {/* Hero Section */}
      <section className="text-center">
        <h1 className="text-4xl md:text-5xl font-bold text-gray-800 dark:text-white mb-6">
          Welcome to Netpick
        </h1>
        <p className="text-lg text-gray-600 dark:text-gray-300 mb-8 max-w-2xl mx-auto">
          Advanced email scraping and analytics platform designed to help you extract,
          analyze, and manage contact data efficiently with powerful automation tools.
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link
            href="/about"
            className="inline-flex items-center justify-center px-6 py-3 text-base font-medium text-white bg-brand-500 rounded-lg hover:bg-brand-600 transition-colors"
          >
            Learn More
          </Link>
          <Link
            href="/contact"
            className="inline-flex items-center justify-center px-6 py-3 text-base font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700 transition-colors"
          >
            Contact Us
          </Link>
        </div>
      </section>

      {/* Features Section */}
      <section>
        <h2 className="text-3xl font-bold text-center text-gray-800 dark:text-white mb-12">
          Key Features
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="w-12 h-12 bg-brand-100 rounded-xl flex items-center justify-center mb-4">
              <svg className="w-6 h-6 text-brand-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h3 className="text-xl font-semibold text-gray-800 dark:text-white mb-2">
              Smart Scraping
            </h3>
            <p className="text-gray-600 dark:text-gray-400">
              Extract email addresses from various sources with intelligent filtering
              and validation to ensure data quality.
            </p>
          </div>

          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="w-12 h-12 bg-brand-100 rounded-xl flex items-center justify-center mb-4">
              <svg className="w-6 h-6 text-brand-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <h3 className="text-xl font-semibold text-gray-800 dark:text-white mb-2">
              Analytics Dashboard
            </h3>
            <p className="text-gray-600 dark:text-gray-400">
              Monitor your scraping performance with real-time metrics and
              comprehensive analytics.
            </p>
          </div>

          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="w-12 h-12 bg-brand-100 rounded-xl flex items-center justify-center mb-4">
              <svg className="w-6 h-6 text-brand-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4 2 2 0 100-4zm0 6v2m0 4v2m-6-4h12M6 12h12" />
              </svg>
            </div>
            <h3 className="text-xl font-semibold text-gray-800 dark:text-white mb-2">
              Pipeline Automation
            </h3>
            <p className="text-gray-600 dark:text-gray-400">
              Create automated workflows to process and validate your scraped
              data with minimal manual intervention.
            </p>
          </div>
        </div>
      </section>

      {/* Stats Preview */}
      <section className="rounded-2xl border border-gray-200 bg-white p-8 dark:border-gray-800 dark:bg-white/[0.03]">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
          <div>
            <div className="text-3xl font-bold text-brand-500 mb-2">10K+</div>
            <div className="text-gray-600 dark:text-gray-400">Contacts Scraped</div>
          </div>
          <div>
            <div className="text-3xl font-bold text-brand-500 mb-2">500+</div>
            <div className="text-gray-600 dark:text-gray-400">Active Pipelines</div>
          </div>
          <div>
            <div className="text-3xl font-bold text-brand-500 mb-2">99%</div>
            <div className="text-gray-600 dark:text-gray-400">Accuracy Rate</div>
          </div>
          <div>
            <div className="text-3xl font-bold text-brand-500 mb-2">24/7</div>
            <div className="text-gray-600 dark:text-gray-400">Monitoring</div>
          </div>
        </div>
      </section>
    </div>
  );
}