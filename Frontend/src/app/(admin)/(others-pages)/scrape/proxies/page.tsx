"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState } from "react";
import { useProxies } from "@/hooks/useProxies";
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import ConfirmModal from "@/components/forms/ConfirmModal";
import ProxyService, { Proxy, ProxyRequest } from "@/services/proxyService";
import { useToast } from "@/context/ToastContext";

export default function Proxies() {
    const [currentPage, setCurrentPage] = useState<number>(1);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isImportModalOpen, setIsImportModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState<Proxy | null>(null);
    const [importText, setImportText] = useState("");
    const { addToast } = useToast();
    const { proxies, loading, error, totalPages, refetch } = useProxies(currentPage);

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
    };

    const handleCreate = () => {
        setSelectedItem(null);
        setIsCreateModalOpen(true);
    };

    const handleEdit = (row: Record<string, unknown>) => {
        const proxy = proxies.find(p => p.id === row.id);
        if (proxy) {
            setSelectedItem(proxy);
            setIsEditModalOpen(true);
        }
    };

    const handleDelete = (row: Record<string, unknown>) => {
        const proxy = proxies.find(p => p.id === row.id);
        if (proxy) {
            setSelectedItem(proxy);
            setIsDeleteModalOpen(true);
        }
    };

    // const handleTest = async (row: Record<string, unknown>) => {
    //     try {
    //         await ProxyService.testProxy(row.id as string);
    //         addToast("success", "Success", "Proxy test started");
    //         await refetch();
    //     } catch {
    //         addToast("error", "Error", "Failed to test proxy");
    //     }
    // };

    const handleCreateSubmit = async (data: Record<string, unknown>) => {
        try {
            const request: ProxyRequest = {
                protocol: data.protocol as ProxyRequest['protocol'],
                host: data.host as string,
                port: parseInt(data.port as string) || 0,
                username: data.username as string,
                password: data.password as string,
                description: data.description as string
            };
            await ProxyService.createProxy(request);
            addToast("success", "Success", "Proxy created successfully");
            setIsCreateModalOpen(false);
            await refetch();
        } catch (err) {
            console.error("Failed to create proxy:", err);
            addToast("error", "Error", "Failed to create proxy");
        }
    };

    const handleEditSubmit = async (data: Record<string, unknown>) => {
        if (!selectedItem) return;
        try {
            const request: ProxyRequest = {
                protocol: data.protocol as ProxyRequest['protocol'],
                host: data.host as string,
                port: parseInt(data.port as string) || 0,
                username: data.username as string,
                password: data.password as string,
                description: data.description as string
            };
            await ProxyService.updateProxy(selectedItem.id, request);
            addToast("success", "Success", "Proxy updated successfully");
            setIsEditModalOpen(false);
            await refetch();
        } catch (err) {
            console.error("Failed to update proxy:", err);
            addToast("error", "Error", "Failed to update proxy");
        }
    };

    const handleDeleteConfirm = async () => {
        if (!selectedItem) return;
        try {
            await ProxyService.deleteProxy(selectedItem.id);
            addToast("success", "Success", "Proxy deleted successfully");
            setIsDeleteModalOpen(false);
            await refetch();
        } catch (err) {
            console.error("Failed to delete proxy:", err);
            addToast("error", "Error", "Failed to delete proxy");
        }
    };

    const handleImport = async () => {
        try {
            const result = await ProxyService.importProxiesFromText(importText);
            addToast("success", "Success", `Imported ${result.imported} proxies`);
            setIsImportModalOpen(false);
            setImportText("");
            await refetch();
        } catch (err) {
            console.error("Failed to import proxies:", err);
            addToast("error", "Error", "Failed to import proxies");
        }
    };

    const handleTestUntested = async () => {
        try {
            await ProxyService.testUntestedProxies();
            addToast("success", "Success", "Testing untested proxies in background");
        } catch (err) {
            console.error("Failed to test untested proxies:", err);
            addToast("error", "Error", "Failed to test untested proxies");
        }
    };

    const handleTestActive = async () => {
        try {
            await ProxyService.testActiveProxies();
            addToast("success", "Success", "Re-testing active proxies in background");
        } catch (err) {
            console.error("Failed to test active proxies:", err);
            addToast("error", "Error", "Failed to test active proxies");
        }
    };

    const columns: ColumnConfig[] = [
        { key: "host", header: "Host", type: "text" },
        { key: "port", header: "Port", type: "text" },
        { key: "protocol", header: "Protocol", type: "text" },
        { key: "status", header: "Status", type: "text" },
        { key: "avgResponseTime", header: "Avg Response (ms)", type: "text" },
        { key: "edit", header: "Edit", type: "edit" },
        { key: "delete", header: "Delete", type: "delete" },
    ];

    const data = proxies.map(proxy => ({
        id: proxy.id,
        host: proxy.host,
        port: proxy.port?.toString() || "-",
        protocol: proxy.protocol || "-",
        status: proxy.status || "UNTESTED",
        avgResponseTime: proxy.avgResponseTimeMs?.toString() || "-",
    }));

    const createFields = [
        {
            name: "protocol", label: "Protocol", type: "select", options: [
                { value: "HTTP", label: "HTTP" },
                { value: "HTTPS", label: "HTTPS" },
                { value: "SOCKS4", label: "SOCKS4" },
                { value: "SOCKS5", label: "SOCKS5" },
            ]
        },
        { name: "host", label: "Host", type: "text", required: true },
        { name: "port", label: "Port", type: "number", required: true },
        { name: "username", label: "Username", type: "text" },
        { name: "password", label: "Password", type: "password" },
        { name: "description", label: "Description", type: "textarea" },
    ];

    const editFields = [
        {
            name: "protocol", label: "Protocol", type: "select", options: [
                { value: "HTTP", label: "HTTP" },
                { value: "HTTPS", label: "HTTPS" },
                { value: "SOCKS4", label: "SOCKS4" },
                { value: "SOCKS5", label: "SOCKS5" },
            ]
        },
        { name: "host", label: "Host", type: "text", required: true },
        { name: "port", label: "Port", type: "number", required: true },
        { name: "username", label: "Username", type: "text" },
        { name: "password", label: "Password", type: "password" },
        { name: "description", label: "Description", type: "textarea" },
    ];

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <>
            <PageBreadcrumb pageTitle="Proxies" />
            <div className="mb-4 flex gap-2 flex-wrap">
                <Button onClick={handleCreate}>Add Proxy</Button>
                <Button onClick={() => setIsImportModalOpen(true)}>Import Proxies</Button>
                <Button onClick={handleTestUntested}>Test Untested</Button>
                <Button onClick={handleTestActive}>Re-test Active</Button>
            </div>
            <DynamicTable
                columns={columns}
                data={data}
                onEdit={handleEdit}
                onDelete={handleDelete}
            />
            <div className="mt-5">
                <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                />
            </div>

            <ModalForm
                isOpen={isCreateModalOpen}
                onCloseAction={() => setIsCreateModalOpen(false)}
                onSubmit={handleCreateSubmit}
                title="Add Proxy"
                fields={createFields}
                submitButtonText="Create"
            />

            <ModalForm
                isOpen={isEditModalOpen}
                onCloseAction={() => setIsEditModalOpen(false)}
                onSubmit={handleEditSubmit}
                title="Edit Proxy"
                fields={editFields}
                initialData={selectedItem ? {
                    protocol: selectedItem.protocol,
                    host: selectedItem.host,
                    port: selectedItem.port?.toString(),
                    username: selectedItem.username || "",
                    description: selectedItem.description || "",
                } : undefined}
                submitButtonText="Update"
            />

            <ConfirmModal
                isOpen={isDeleteModalOpen}
                onCloseAction={() => setIsDeleteModalOpen(false)}
                onConfirm={handleDeleteConfirm}
                title="Delete Proxy"
                message={`Are you sure you want to delete the proxy "${selectedItem?.host}:${selectedItem?.port}"?`}
            />

            {/* Import Modal */}
            {isImportModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
                    <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-lg">
                        <h2 className="text-xl font-semibold mb-4">Import Proxies</h2>
                        <p className="text-sm text-gray-600 dark:text-gray-400 mb-2">
                            Enter proxies (one per line) in format: protocol://user:pass@host:port or host:port
                        </p>
                        <textarea
                            className="w-full h-48 p-2 border rounded dark:bg-gray-700 dark:border-gray-600"
                            value={importText}
                            onChange={(e) => setImportText(e.target.value)}
                            placeholder="socks5://user:pass@192.168.1.1:1080&#10;192.168.1.2:8080"
                        />
                        <div className="flex justify-end gap-2 mt-4">
                            <Button onClick={() => setIsImportModalOpen(false)}>Cancel</Button>
                            <Button onClick={handleImport}>Import</Button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}
