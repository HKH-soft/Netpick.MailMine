"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState } from "react";
import { useScrapeMessages } from "@/hooks/useScrapeMessages";
import { EmailMessage } from "@/services/emailMessageService";

export default function Messages() {
  const [currentPage, setCurrentPage] = useState<number>(0);
  const { messages, loading, error, totalPages } = useScrapeMessages(currentPage);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const columns: ColumnConfig[] = [
    { key: "senderEmail", header: "From", type: "text" },
    { key: "subject", header: "Subject", type: "text" },
    { key: "bodyText", header: "Preview", type: "text" },
    { key: "status", header: "Status", type: "text" },
    { key: "receivedAt", header: "Received", type: "text" },
  ];

  const data = messages.map((msg: EmailMessage) => ({
    id: msg.id,
    senderEmail: msg.senderName ? `${msg.senderName} <${msg.senderEmail}>` : msg.senderEmail || "-",
    subject: msg.subject || "-",
    bodyText: msg.bodyText ? (msg.bodyText.length > 60 ? msg.bodyText.substring(0, 60) + "..." : msg.bodyText) : "-",
    status: msg.status || "-",
    receivedAt: msg.receivedAt ? new Date(msg.receivedAt).toLocaleString() : "-",
  }));

  if (loading) {
    return <div className="flex items-center justify-center p-8">
      <div className="text-gray-500 dark:text-gray-400">Loading messages...</div>
    </div>;
  }

  if (error) {
    return <div className="flex items-center justify-center p-8">
      <div className="text-red-500 dark:text-red-400">Error: {error}</div>
    </div>;
  }

  return (
    <div>
      <PageBreadcrumb pageTitle="Messages" />
      <div className="space-y-6">
        {messages.length === 0 ? (
          <p className="text-gray-500 dark:text-gray-400 text-center py-4">No messages found.</p>
        ) : (
          <>
            <DynamicTable columns={columns} data={data} />
            <div className="mt-5">
              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
              />
            </div>
          </>
        )}
      </div>
    </div>
  );
}
