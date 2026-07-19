"use client";
import React, { useState } from "react";
import dynamic from "next/dynamic";

const ReactApexChart = dynamic(() => import("react-apexcharts"), { ssr: false });

const ranges = ["7D", "30D", "90D"];

export default function ChartWidget() {
  const [range, setRange] = useState("30D");

  const options = {
    chart: { type: "area" as const, toolbar: { show: false }, background: "transparent", fontFamily: "Inter, sans-serif" },
    colors: ["#22c55e"],
    stroke: { curve: "smooth" as const, width: 2 },
    fill: { type: "gradient" as const, gradient: { shadeIntensity: 1, opacityFrom: 0.25, opacityTo: 0 } },
    xaxis: { categories: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"].slice(0, range === "7D" ? 7 : range === "30D" ? 12 : 12), labels: { style: { colors: "#666", fontSize: "11px" } }, axisBorder: { show: false }, axisTicks: { show: false } },
    yaxis: { labels: { style: { colors: "#666", fontSize: "11px" }, formatter: (v: number) => v >= 1000 ? `${(v / 1000).toFixed(0)}k` : v.toString() } },
    grid: { borderColor: "rgba(255,255,255,0.04)", strokeDashArray: 3 },
    tooltip: { theme: "dark" as const, style: { fontSize: "12px" } },
    dataLabels: { enabled: false },
  };

  const series = [{
    name: "Contacts",
    data: range === "7D" ? [320, 410, 380, 520, 480, 610, 580] :
          range === "30D" ? [210, 320, 280, 410, 380, 520, 480, 610, 580, 720, 690, 810] :
          [1200, 1400, 1100, 1600, 1500, 1800, 1700, 2100, 2000, 2400, 2300, 2700],
  }];

  return (
    <div className="h-full flex flex-col">
      <div className="flex items-center gap-1 mb-4">
        {ranges.map((r) => (
          <button
            key={r}
            onClick={() => setRange(r)}
            className={`px-3 py-1 text-[11px] font-medium rounded-full transition-all duration-200 ${
              range === r
                ? "bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400 border border-green-200 dark:border-green-500/20"
                : "text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 border border-transparent"
            }`}
          >
            {r}
          </button>
        ))}
      </div>
      <div className="flex-1 min-h-0">
        <ReactApexChart options={options} series={series} type="area" height="100%" />
      </div>
    </div>
  );
}
