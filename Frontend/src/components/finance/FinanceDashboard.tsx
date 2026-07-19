// FinanceDashboard.tsx
"use client";
import React from "react";
import { useFinanceSummary } from "@/hooks/useInvoices";
import { FiDollarSign, FiFileText, FiTrendingUp, FiTrendingDown } from "react-icons/fi";

export default function FinanceDashboard() {
  const { summary, loading, error } = useFinanceSummary();

  if (loading) {
    return <div className="text-center py-8">Loading...</div>;
  }

  if (error) {
    return <div className="text-center py-8 text-red-500">{error}</div>;
  }

  const stats = [
    {
      title: "Total Revenue",
      value: `$${summary?.totalRevenue?.toFixed(2) || "0.00"}`,
      icon: <FiDollarSign className="w-6 h-6 text-green-500" />,
      detail: "",
    },
    {
      title: "Total Expenses",
      value: `$${summary?.totalExpenses?.toFixed(2) || "0.00"}`,
      icon: <FiTrendingDown className="w-6 h-6 text-red-500" />,
      detail: "",
    },
    {
      title: "Profit",
      value: `$${summary?.profit?.toFixed(2) || "0.00"}`,
      icon: <FiTrendingUp className="w-6 h-6 text-brand-500" />,
      detail: "",
    },
    {
      title: "Total Invoices",
      value: summary?.totalInvoices?.toString() || "0",
      icon: <FiFileText className="w-6 h-6 text-blue-500" />,
      detail: `${summary?.paidInvoices || 0} paid`,
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      {stats.map((stat, index) => (
        <div key={index} className="rounded-lg border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-5">
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-500 dark:text-gray-400">{stat.title}</span>
            {stat.icon}
          </div>
          <div className="mt-3">
            <span className="text-2xl font-bold text-gray-800 dark:text-white/90">{stat.value}</span>
            {stat.detail && <span className="text-sm text-gray-500 ml-2">{stat.detail}</span>}
          </div>
        </div>
      ))}
    </div>
  );
}
