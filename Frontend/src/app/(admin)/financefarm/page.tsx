"use client";

import React, { useState } from "react";
import FinanceDashboard from "@/components/finance/FinanceDashboard";
import InvoiceList from "@/components/finance/InvoiceList";
import { useInvoices } from "@/hooks/useInvoices";
import InvoiceService, { InvoiceStatus } from "@/services/invoiceService";
import { useToast } from "@/context/ToastContext";

export default function FinanceFarmPage() {
  const [view, setView] = useState<"dashboard" | "invoices">("dashboard");
  const { invoices, loading, refetch } = useInvoices(1);
  const { addToast } = useToast();

  const handleDelete = async (id: string) => {
    if (confirm("Are you sure you want to delete this invoice?")) {
      try {
        await InvoiceService.deleteInvoice(id);
        addToast("success", "Success", "Invoice deleted successfully");
        await refetch();
      } catch (err) {
        console.error("Failed to delete invoice:", err);
        addToast("error", "Error", "Failed to delete invoice");
      }
    }
  };

  const handleStatusChange = async (id: string, status: InvoiceStatus) => {
    try {
      await InvoiceService.updateInvoiceStatus(id, status);
      addToast("success", "Success", "Invoice status updated successfully");
      await refetch();
    } catch (err) {
      console.error("Failed to update invoice status:", err);
      addToast("error", "Error", "Failed to update invoice status");
    }
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