"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState } from "react";
import { useContacts } from "@/hooks/useContacts";
import ConfirmModal from "@/components/forms/ConfirmModal";
import ContactService from "@/services/contactService";
import { Contact } from "@/services/contactService";
import { useToast } from "@/context/ToastContext";

export default function Contacts() {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<Contact | null>(null);
  const { addToast } = useToast();
  const { contacts, loading, error, totalPages, refetch } = useContacts(currentPage);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleDelete = (row: Record<string, unknown>) => {
    const contact = contacts.find(c => c.id === row.id);
    if (contact) {
      setSelectedItem(contact);
      setIsDeleteModalOpen(true);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedItem) return;
    try {
      await ContactService.deleteContact(selectedItem.id);
      addToast("success", "Success", "Contact deleted successfully");
      setIsDeleteModalOpen(false);
      await refetch();
    } catch (err) {
      console.error("Failed to delete contact:", err);
      addToast("error", "Error", "Failed to delete contact");
    }
  };

  const columns: ColumnConfig[] = [
    { key: "id", header: "ID", type: "text" },
    { key: "emails", header: "Emails", type: "text" },
    { key: "createdAt", header: "Created At", type: "text" },
    { key: "delete", header: "Delete", type: "delete" },
  ];

  const data = contacts.map(contact => ({
    id: contact.id,
    emails: contact.emails?.join(", ") || "-",
    createdAt: contact.createdAt ? new Date(contact.createdAt).toLocaleString() : "-",
  }));

  if (loading) {
    return <div className="flex items-center justify-center p-8">
      <div className="text-gray-500 dark:text-gray-400">Loading contacts...</div>
    </div>;
  }

  if (error) {
    return <div className="flex items-center justify-center p-8">
      <div className="text-red-500 dark:text-red-400">Error: {error}</div>
    </div>;
  }

  return (
    <div>
      <PageBreadcrumb pageTitle="Contacts" />
      <div className="space-y-6">
        {contacts.length === 0 ? (
          <p className="text-gray-500 dark:text-gray-400 text-center py-4">No contacts found.</p>
        ) : (
          <>
            <DynamicTable
              columns={columns}
              data={data}
              onDelete={handleDelete}
            />
            <div className="mt-5">
              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
              />
            </div>
          </>
        )}
      </div>

      <ConfirmModal
        isOpen={isDeleteModalOpen}
        onCloseAction={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteConfirm}
        title="Delete Contact"
        message={`Are you sure you want to delete this contact?`}
      />
    </div>
  );
}


