// Email Auth Check Page
import type { Metadata } from "next";
import EmailAuthCheck from "@/components/email/EmailAuthCheck";
import React from "react";

export const metadata: Metadata = {
  title: "Email Auth Check - Netpick",
  description: "Validate SPF/DKIM/DMARC records for email domains",
};

export default function EmailAuthPage() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <div className="rounded-sm border border-gray-200 bg-white px-6 py-4 shadow-default dark:border-gray-700 dark:bg-gray-900">
          <EmailAuthCheck />
        </div>
      </div>
    </div>
  );
}



