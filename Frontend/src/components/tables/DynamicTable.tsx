// DynamicTable.tsx
"use client";
import React from "react";
import Image from "next/image";
import Badge from "../ui/badge/Badge";
import {
    Table,
    TableBody,
    TableCell,
    TableHeader,
    TableRow,
} from "../ui/table";
import Button from "@/components/ui/button/Button";


/* ---------- Types ---------- */
export type ColumnType =
    | "profile"
    | "text"
    | "status"
    | "custom"
    | "edit"
    | "delete";

export interface ColumnConfig {
    key: string;
    header: string;
    type: ColumnType;
    render?: (value: unknown, row: Record<string, unknown>) => React.ReactNode;
}

interface DynamicTableProps {
    columns: ColumnConfig[];
    data: Record<string, unknown>[];
    onEdit?: (row: Record<string, unknown>) => void;
    onDelete?: (row: Record<string, unknown>) => void;
}

/* ---------- Component ---------- */
export default function DynamicTable({
    columns,
    data,
    onEdit,
    onDelete,
}: DynamicTableProps) {
    const getCellContent = (type: ColumnType, value: unknown, row: Record<string, unknown>) => {
        switch (type) {
            case "profile":
                // Type assertion to handle the unknown type
                const profileValue = value as { image?: string; name?: string; email?: string } | null | undefined;
                return (
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 overflow-hidden rounded-full">
                            <Image
                                width={40}
                                height={40}
                                src={profileValue?.image || "/images/avatars/avatar-default.png"}
                                alt={profileValue?.name || "Profile"}
                            />
                        </div>
                        <div>
                            <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                                {profileValue?.name}
                            </span>
                            <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                                {profileValue?.email}
                            </span>
                        </div>
                    </div>
                );

            case "status": {
                // Convert value to string for comparison and display
                const valueStr = String(value);
                const color =
                    valueStr === "Active"
                        ? "success"
                        : valueStr === "Pending"
                            ? "warning"
                            : "error";
                return (
                    <Badge size="sm" color={color}>
                        {valueStr}
                    </Badge>
                );
            }

            case "text":
                return (
                    <span className="text-gray-500 text-theme-sm dark:text-gray-400">
                        {String(value)}
                    </span>
                );

            case "edit":
                return (
                    <Button
                        size="sm"
                        variant="outline"
                        className="border-gray-200 text-gray-700 hover:bg-gray-100 dark:border-white/[0.08] dark:text-gray-300 dark:hover:bg-white/[0.08] w-full"
                        onClick={() => onEdit && onEdit(row)}
                    >
                        Edit
                    </Button>
                );

            case "delete":
                return (
                    <Button
                        size="sm"
                        variant="primary"
                        className="bg-red-500 hover:bg-red-600 text-white dark:bg-red-600 dark:hover:bg-red-700 w-full"
                        onClick={() => onDelete && onDelete(row)}
                    >
                        Delete
                    </Button>
                );

            case "custom":
            default:
                // Convert value to string to ensure it's a valid ReactNode
                return String(value);
        }
    };

    return (
        <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
            <div className="max-w-full overflow-x-auto">
                <div className="min-w-[1102px]">
                    <Table>
                        {/* Table Header */}
                        <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
                            <TableRow>
                                {columns.map((c) => (
                                    <TableCell
                                        key={c.key}
                                        isHeader
                                        className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                                    >
                                        {c.header}
                                    </TableCell>
                                ))}
                            </TableRow>
                        </TableHeader>

                        {/* Table Body */}
                        <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                            {data.map((row, rIdx) => (
                                <TableRow key={rIdx}>
                                    {columns.map((col) => (
                                        <TableCell
                                            key={col.key}
                                            className="px-4 py-3 text-start text-theme-sm text-gray-500 dark:text-gray-400"
                                        >
                                            {col.render
                                                ? col.render(row[col.key], row)
                                                : getCellContent(col.type, row[col.key], row)}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </div>
            </div>
        </div>
    );
}