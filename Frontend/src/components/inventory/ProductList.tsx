// ProductList.tsx
"use client";
import React from "react";
import { Product } from "@/services/productService";

interface ProductListProps {
  products: Product[];
  loading: boolean;
  onEdit?: (product: Product) => void;
  onDelete?: (id: string) => void;
  onAdjustStock?: (id: string) => void;
}

export default function ProductList({ products, loading, onEdit, onDelete, onAdjustStock }: ProductListProps) {
  if (loading) {
    return <div className="text-center py-8 text-gray-500">Loading products...</div>;
  }

  if (!products.length) {
    return <div className="text-center py-8 text-gray-500">No products found.</div>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead>
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Product</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">SKU</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Quantity</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Unit Price</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Last Updated</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
          {products.map((product) => {
            const isLow = product.quantity <= (product.minQuantity || 0);
            return (
              <tr key={product.id} className="hover:bg-gray-50 dark:hover:bg-white/[0.02]">
                <td className="px-4 py-3 text-sm text-gray-800 dark:text-white/90">{product.productName}</td>
                <td className="px-4 py-3 text-sm text-gray-500">{product.sku}</td>
                <td className="px-4 py-3 text-sm">
                  <span className={isLow ? "text-red-600 font-medium" : "text-gray-800 dark:text-white/90"}>
                    {product.quantity}{isLow && " (Low)"}
                  </span>
                </td>
                <td className="px-4 py-3 text-sm font-medium text-gray-800 dark:text-white/90">${product.unitPrice?.toFixed(2) || "0.00"}</td>
                <td className="px-4 py-3 text-sm text-gray-500">{product.lastStockUpdate}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    <button onClick={() => onAdjustStock?.(product.id)} className="text-sm text-brand-500 hover:underline">Adjust</button>
                    <button onClick={() => onEdit?.(product)} className="text-sm text-brand-500 hover:underline">Edit</button>
                    <button onClick={() => onDelete?.(product.id)} className="text-sm text-red-500 hover:underline">Delete</button>
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}