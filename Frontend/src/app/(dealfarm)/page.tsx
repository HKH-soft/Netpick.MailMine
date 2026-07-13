import type { Metadata } from "next";
import React from "react";

export const metadata: Metadata = {
  title: "DealPick - CRM and Sales Pipeline",
  description: "Manage deals, contacts, and sales pipeline",
};

export default function DealFarmPage() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <h1 className="text-2xl font-bold">DealPick - CRM and Sales Pipeline</h1>
        <p className="text-gray-500 dark:text-gray-400 mt-2">
          Manage deals, contacts, and sales pipeline
        </p>
      </div>
    </div>
  );
}