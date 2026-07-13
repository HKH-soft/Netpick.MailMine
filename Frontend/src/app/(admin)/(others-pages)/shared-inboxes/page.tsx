// Shared Inboxes Page
import type { Metadata } from "next";
import SharedInboxView from "@/components/email/SharedInboxView";
import React from "react";

export const metadata: Metadata = {
  title: "Shared Inboxes - Netpick",
  description: "Manage shared inbox configurations",
};

export default function SharedInboxesPage() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <div className="rounded-sm border border-stroke bg-white px-6 py-4 shadow-default dark:border-strokedark dark:bg-boxdark">
          <SharedInboxView />
        </div>
      </div>
    </div>
  );
}



