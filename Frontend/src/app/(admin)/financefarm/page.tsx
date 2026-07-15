"use client";

import React, { useState } from "react";
import FinanceDashboard from "@/components/finance/FinanceDashboard";
import InvoiceList from "@/components/finance/InvoiceList";
import { useInvoices } from "@/hooks/useInvoices";

export default function FinanceFarmPage() {
  const [view, setView] = useState<"dashboard" | "invoices">("dashboard");
  const { invoices, loading, refetch } = useInvoices(1);

  const handleDelete = async () => {
    if (confirm("Are you sure you want to delete this invoice?")) {
      refetch();
    }
  };

  const handleStatusChange = async () => {
    // Would call InvoiceService.updateInvoiceStatus(id, status)
    refetch();
  };

  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12 flex items-center justify-between">
        <h1 className="text-2xl font-bold">FinancePick - Accounting and Invoicing</h1>
        <div className="flex gap-2">
          <button
            onClick={() => setView("dashboard")}
            className={`px-3 py-1 text-sm rounded ${view === "dashboard" ? "bg-brand-500 text-white" : "bg-gray-200"}`}
          >
            Dashboard
          </button>
          <button
            onClick={() => setView("invoices")}
            className={`px-3 py-1 text-sm rounded ${view === "invoices" ? "bg-brand-500 text-white" : "bg-gray-200"}`}
          >
            Invoices
          </button>
        </div>
      </div>

      {view === "dashboard" && (
        <div className="col-span-12">
          <FinanceDashboard />
        </div>
      )}

      {view === "invoices" && (
        <div className="col-span-12">
          <InvoiceList
            invoices={invoices}
            loading={loading}
            onDelete={handleDelete}
            onStatusChange={handleStatusChange}
          />
        </div>
      )}
    </div>
  );
}