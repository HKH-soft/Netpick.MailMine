// Follow-ups Page
import type { Metadata } from "next";
import FollowUpDashboard from "@/components/email/FollowUpDashboard";
import React from "react";

export const metadata: Metadata = {
  title: "Follow-ups - Netpick",
  description: "Email follow-up detection dashboard",
};

export default function FollowUpsPage() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <div className="rounded-sm border border-stroke bg-white px-6 py-4 shadow-default dark:border-strokedark dark:bg-boxdark">
          <FollowUpDashboard />
        </div>
      </div>
    </div>
  );
}



