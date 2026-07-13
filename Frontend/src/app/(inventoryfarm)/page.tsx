import type { Metadata } from "next";
import React from "react";

export const metadata: Metadata = {
  title: "InventoryPick - Stock and Warehouse",
  description: "Manage inventory, stock levels, and warehouses",
};

export default function InventoryFarmPage() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <h1 className="text-2xl font-bold">InventoryPick - Stock and Warehouse</h1>
        <p className="text-gray-500 dark:text-gray-400 mt-2">
          Manage inventory, stock levels, and warehouses
        </p>
      </div>
    </div>
  );
}