// InvoiceList.tsx
"use client";
import React from "react";
import { Invoice, InvoiceStatus } from "@/services/invoiceService";

interface InvoiceListProps {
  invoices: Invoice[];
  loading: boolean;
  onEdit?: (invoice: Invoice) => void;
  onDelete?: (id: string) => void;
  onStatusChange?: (id: string, status: InvoiceStatus) => void;
}

const statusColors: Record<InvoiceStatus, string> = {
  [InvoiceStatus.DRAFT]: "bg-gray-100 text-gray-800",
  [InvoiceStatus.SENT]: "bg-blue-100 text-blue-800",
  [InvoiceStatus.PAID]: "bg-green-100 text-green-800",
  [InvoiceStatus.OVERDUE]: "bg-red-100 text-red-800",
  [InvoiceStatus.CANCELLED]: "bg-yellow-100 text-yellow-800",
};

export default function InvoiceList({ invoices, loading, onEdit, onDelete }: InvoiceListProps) {
  if (loading) {
    return <div className="text-center py-8 text-gray-500">Loading invoices...</div>;
  }

  if (!invoices.length) {
    return <div className="text-center py-8 text-gray-500">No invoices found.</div>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead>
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Invoice #</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Customer</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Issue Date</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Due Date</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
          {invoices.map((invoice) => (
            <tr key={invoice.id} className="hover:bg-gray-50 dark:hover:bg-gray-800">
              <td className="px-4 py-3 text-sm text-gray-800 dark:text-white/90">{invoice.invoiceNumber}</td>
              <td className="px-4 py-3 text-sm text-gray-800 dark:text-white/90">{invoice.customerName}</td>
              <td className="px-4 py-3 text-sm font-medium text-gray-800 dark:text-white/90">${invoice.totalAmount?.toFixed(2) || "0.00"}</td>
              <td className="px-4 py-3">
                <span className={`px-2 py-1 text-xs rounded-full ${statusColors[invoice.status] || "bg-gray-100"}`}>
                  {invoice.status?.replace("_", " ")}
                </span>
              </td>
              <td className="px-4 py-3 text-sm text-gray-500">{invoice.issueDate}</td>
              <td className="px-4 py-3 text-sm text-gray-500">{invoice.dueDate}</td>
              <td className="px-4 py-3">
                <div className="flex gap-2">
                  <button onClick={() => onEdit?.(invoice)} className="text-sm text-brand-500 hover:underline">Edit</button>
                  <button onClick={() => onDelete?.(invoice.id)} className="text-sm text-red-500 hover:underline">Delete</button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}