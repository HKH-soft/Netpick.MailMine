import type { Metadata } from "next";
import React from "react";

export const metadata: Metadata = {
  title: "FinancePick - Accounting and Invoicing",
  description: "Manage invoices, transactions, and accounting",
};

export default function FinanceFarmPage() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <h1 className="text-2xl font-bold">FinancePick - Accounting and Invoicing</h1>
        <p className="text-gray-500 dark:text-gray-400 mt-2">
          Manage invoices, transactions, and accounting
        </p>
      </div>
    </div>
  );
}