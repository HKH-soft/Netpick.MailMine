// Campaigns Page
import type { Metadata } from "next";
import CampaignBuilder from "@/components/email/CampaignBuilder";
import React from "react";

export const metadata: Metadata = {
  title: "Campaigns - Netpick",
  description: "Email campaign management",
};

export default function CampaignsPage() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <div className="rounded-sm border border-stroke bg-white px-6 py-4 shadow-default dark:border-strokedark dark:bg-boxdark">
          <CampaignBuilder />
        </div>
      </div>
    </div>
  );
}



