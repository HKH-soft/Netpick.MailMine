import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import { Metadata } from "next";
import React from "react";

export const metadata: Metadata = {
  title: "Statistics | MailMine",
  description: "Application Statistics and Metrics",
};

export default function StatisticsPage() {
  return (
    <div>
      <PageBreadcrumb pageTitle="Statistics" />
      <div className="min-h-[400px] flex items-center justify-center rounded-2xl border border-gray-200 bg-white px-5 py-7 dark:border-gray-800 dark:bg-white/[0.03] xl:px-10 xl:py-12">
        <div className="mx-auto w-full max-w-[630px] text-center">
          <h3 className="mb-4 font-semibold text-gray-800 text-theme-xl dark:text-white/90 sm:text-2xl">
            Advanced Analytics in Grafana
          </h3>
          <p className="mb-6 text-sm text-gray-500 dark:text-gray-400 sm:text-base">
            We have moved our detailed application metrics, performance tracking, and historical statistics to Grafana.
            This allows for more powerful visualization, alerting, and deep-dive analysis of your scraping operations.
          </p>

          <div className="p-4 mb-6 rounded-lg bg-gray-50 dark:bg-gray-800">
            <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
              Prometheus Endpoint: <code className="px-2 py-1 ml-1 text-xs bg-gray-200 rounded dark:bg-gray-700">/actuator/prometheus</code>
            </p>
          </div>

          <a
            href="http://localhost:3000"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center justify-center px-6 py-3 text-base font-medium text-white transition-colors duration-200 rounded-lg bg-brand-500 hover:bg-brand-600"
          >
            Open Grafana Dashboard
          </a>
        </div>
      </div>
    </div>
  );
}
