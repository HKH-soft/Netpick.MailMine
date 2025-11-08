"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState } from "react";
import { useContacts } from "@/hooks/useContacts";
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import ConfirmModal from "@/components/forms/ConfirmModal";
import ContactService from "@/services/contactService";
import { Contact } from "@/services/contactService";
import { useToast } from "@/context/ToastContext";

// Define types for our data

export default function Contacts() {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<Contact | null>(null);
  const { addToast } = useToast();
  const { contacts, loading, error, totalPages, refetch } = useContacts(currentPage);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleCreate = () => {
    setSelectedItem(null);
    setIsCreateModalOpen(true);
  };

  const handleEdit = (row: Record<string, unknown>) => {
    setSelectedItem({
      id: row.id as string,
      names: [row.name as string],
      emails: row.email ? [row.email as string] : [],
      phoneNumbers: row.phone ? [row.phone as string] : [],
      linkedInUrls: [],
      twitterHandles: [],
      githubProfiles: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
    setIsEditModalOpen(true);
  };

  const handleDelete = (row: Record<string, unknown>) => {
    setSelectedItem({
      id: row.id as string,
      names: [row.name as string],
      emails: row.email ? [row.email as string] : [],
      phoneNumbers: row.phone ? [row.phone as string] : [],
      linkedInUrls: [],
      twitterHandles: [],
      githubProfiles: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
    setIsDeleteModalOpen(true);
  };

  const handleCreateSubmit = async (data: Record<string, unknown>) => {
    try {
      // In a real app, you would call an API to create a contact
      console.log("Creating contact:", data);
      addToast("success", "Success", "Contact created successfully");
      // Close the modal
      setIsCreateModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to create contact:", err);
      addToast("error", "Error", "Failed to create contact");
    }
  };

  const handleEditSubmit = async (data: Record<string, unknown>) => {
    if (!selectedItem) return;
    try {
      // In a real app, you would call an API to update the contact
      console.log("Updating contact:", data);
      addToast("success", "Success", "Contact updated successfully");
      // Close the modal
      setIsEditModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to update contact:", err);
      addToast("error", "Error", "Failed to update contact");
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedItem) return;
    try {
      await ContactService.deleteContact(selectedItem.id);
      addToast("success", "Success", "Contact deleted successfully");
      // Close the modal
      setIsDeleteModalOpen(false);
      // Refetch the data
      await refetch();
    } catch (err) {
      console.error("Failed to delete contact:", err);
      addToast("error", "Error", "Failed to delete contact");
    }
  };

  const columns = [
    { key: "name", header: "Name", type: "text" },
    { key: "emails", header: "Emails", type: "text" },
    { key: "phones", header: "Phones", type: "text" },
    { key: "edit", header: "Edit", type: "edit" },
    { key: "delete", header: "Delete", type: "delete" },
  ] as const satisfies ColumnConfig[];

  // Transform data to match the table structure
  const data = contacts.map(contact => ({
    id: contact.id,
    name: contact.names.join(", ") || "Unknown",
    emails: contact.emails.join(", ") || "-",
    phones: contact.phoneNumbers.join(", ") || "-",
  }));

  const createFields = [
    { name: "name", label: "Name", type: "text", required: true },
    { name: "email", label: "Email", type: "email" },
    { name: "phone", label: "Phone", type: "text" },
  ];

  const editFields = [
    { name: "name", label: "Name", type: "text", required: true },
    { name: "email", label: "Email", type: "email" },
    { name: "phone", label: "Phone", type: "text" },
  ];

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <>
      <PageBreadcrumb pageTitle="Contacts" />
      <div className="mb-4">
        <Button onClick={handleCreate}>Create Contact</Button>
      </div>
      <DynamicTable columns={columns} data={data} onEdit={handleEdit} onDelete={handleDelete} />
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
        title="Create Contact"
        fields={createFields}
        submitButtonText="Create"
      />

      <ModalForm
        isOpen={isEditModalOpen}
        onCloseAction={() => setIsEditModalOpen(false)}
        onSubmit={handleEditSubmit}
        title="Edit Contact"
        fields={editFields}
        initialData={selectedItem ? (selectedItem as unknown as Record<string, unknown>) : undefined}
        submitButtonText="Update"
      />

      <ConfirmModal
        isOpen={isDeleteModalOpen}
        onCloseAction={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteConfirm}
        title="Delete Contact"
        message={`Are you sure you want to delete the contact "${selectedItem?.names[0]}"?`}
      />
    </>
  );
}