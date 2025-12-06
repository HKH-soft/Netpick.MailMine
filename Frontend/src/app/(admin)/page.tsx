import type { Metadata } from "next";
import { DashboardMetrics } from "@/components/dashboard/DashboardMetrics";
import RecentScrapeJobs from "@/components/dashboard/RecentScrapeJobs";
import RecentContacts from "@/components/dashboard/RecentContacts";
import React from "react";

export const metadata: Metadata = {
  title: "MailMine Dashboard",
  description: "MailMine Admin Dashboard",
};

export default function Dashboard() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <DashboardMetrics />
      </div>

      <div className="col-span-12 lg:col-span-6">
        <RecentScrapeJobs />
      </div>

      <div className="col-span-12 lg:col-span-6">
        <RecentContacts />
      </div>
    </div>
  );
}
