"use client";

import React from "react";
import ProductList from "@/components/inventory/ProductList";
import { useProducts } from "@/hooks/useProducts";

export default function InventoryFarmPage() {
  const { products, loading, refetch } = useProducts(1);

  const handleDelete = async () => {
    if (confirm("Are you sure you want to delete this product?")) {
      // Would call ProductService.deleteProduct(id)
      refetch();
    }
  };

  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <h1 className="text-2xl font-bold">InventoryPick - Stock and Warehouse</h1>
        <p className="text-gray-500 dark:text-gray-400 mt-2">
          Manage inventory, stock levels, and warehouses
        </p>
      </div>

      <div className="col-span-12">
        <ProductList
          products={products}
          loading={loading}
          onDelete={handleDelete}
        />
      </div>
    </div>
  );
}