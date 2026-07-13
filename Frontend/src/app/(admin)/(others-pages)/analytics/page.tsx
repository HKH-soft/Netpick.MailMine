// Analytics Page
import type { Metadata } from "next";
import AnalyticsDashboard from "@/components/email/AnalyticsDashboard";
import React from "react";

export const metadata: Metadata = {
  title: "Analytics - Netpick",
  description: "Email analytics and dashboard",
};

export default function AnalyticsPage() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <div className="rounded-sm border border-stroke bg-white px-6 py-4 shadow-default dark:border-strokedark dark:bg-boxdark">
          <AnalyticsDashboard />
        </div>
      </div>
    </div>
  );
}



