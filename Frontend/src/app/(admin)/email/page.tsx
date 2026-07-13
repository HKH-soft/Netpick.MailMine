import type { Metadata } from "next";
import EmailInbox from "@/components/email/EmailInbox";
import React from "react";

export const metadata: Metadata = {
  title: "Email Inbox - MailMine",
  description: "View and manage incoming emails",
};

export default function EmailPage() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <div className="rounded-sm border border-stroke bg-white px-6 py-4 shadow-default dark:border-strokedark dark:bg-boxdark">
          <h2 className="text-xl font-semibold text-black dark:text-white mb-4">
            Email Inbox
          </h2>
          <EmailInbox />
        </div>
      </div>
    </div>
  );
}