"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ProtectedRoute from "@/components/common/ProtectedRoute";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState, useEffect } from "react";
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import ConfirmModal from "@/components/forms/ConfirmModal";
import { useUsers } from "@/hooks/useUsers";
import AuthService from "@/services/authService";
import api from "@/services/api";
import userService, { CreateUserRequest, User } from "@/services/userService";
import { useToast } from "@/context/ToastContext";

export default function Users() {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<User | null>(null);
  const [isSuperAdmin, setIsSuperAdmin] = useState<boolean>(false);
  const { addToast } = useToast();
  const { users, loading, error, totalPages, refetch } = useUsers(currentPage);

  // Check if current user is super admin
  useEffect(() => {
    const token = AuthService.getToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setIsSuperAdmin(payload.role === 'SUPER_ADMIN');
      } catch (e) {
        console.error("Error parsing token", e);
      }
    }
  }, []);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleCreate = () => {
    setSelectedItem(null);
    setIsCreateModalOpen(true);
  };

  const handleEdit = (row: Record<string, unknown>) => {
    if (!isSuperAdmin) {
      setSelectedItem({
        id: row.id as string,
        name: row.name as string,
        email: row.email as string,
        role: row.role as string,
        preference: '',
        profileImageId: '',
        created_at: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        lastLoginAt: new Date().toISOString()
      });
      setIsEditModalOpen(true);
    }
  };

  const handleDelete = (row: Record<string, unknown>) => {
    if (!isSuperAdmin) {
      setSelectedItem({
        id: row.id as string,
        name: row.name as string,
        email: row.email as string,
        role: row.role as string,
        preference: '',
        profileImageId: '',
        created_at: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        lastLoginAt: new Date().toISOString()
      });
      setIsDeleteModalOpen(true);
    }
  };

  const handleCreateSubmit = async (data: Record<string, unknown>) => {
    try {
      const createData: CreateUserRequest = {
        name: data.name as string,
        email: data.email as string,
        password: data.password as string
      };
      
      // If creating an admin, use admin endpoint
      if (data.isAdmin) {
        await userService.createAdmin(createData);
      } else {
        await userService.createUser(createData);
      }
      
      addToast("success", "Success", "User created successfully");
      
      // Close the modal
      setIsCreateModalOpen(false);
      
      // Refetch the user list instead of changing state
      await refetch();
    } catch (err) {
      console.error("Failed to create user:", err);
      addToast("error", "Error", "Failed to create user");
    }
  };

  const handleEditSubmit = async (data: Record<string, unknown>) => {
    if (!selectedItem) return;
    try {
      await api.put(`/users/${selectedItem.id}`, data);
      addToast("success", "Success", "User updated successfully");
      // Close the modal
      setIsEditModalOpen(false);
      // Refetch the user list instead of changing state
      await refetch();
    } catch (err) {
      console.error("Failed to update user:", err);
      addToast("error", "Error", "Failed to update user");
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedItem) return;
    try {
      await api.delete(`/users/${selectedItem.id}`);
      addToast("success", "Success", "User deleted successfully");
      // Close the modal
      setIsDeleteModalOpen(false);
      // Refetch the user list instead of changing state
      await refetch();
    } catch (err) {
      console.error("Failed to delete user:", err);
      addToast("error", "Error", "Failed to delete user");
    }
  };

  // Define columns with conditional edit/delete based on user role
  const columns: ColumnConfig[] = [
    { key: "name", header: "Name", type: "text" },
    { key: "email", header: "Email", type: "text" },
    { key: "role", header: "Role", type: "text" },
  ];

  // Only show edit and delete buttons if user is not super admin
  if (!isSuperAdmin) {
    columns.push(
      { key: "edit", header: "Edit", type: "edit" },
      { key: "delete", header: "Delete", type: "delete" }
    );
  }

  // Transform data to match the table structure
  const data = users.map(user => ({
    id: user.id,
    name: user.name,
    email: user.email,
    role: user.role,
  }));

  const createFields = [
    { name: "name", label: "Name", type: "text", required: true },
    { name: "email", label: "Email", type: "email", required: true },
    { name: "password", label: "Password", type: "password", required: true },
    { name: "isAdmin", label: "Admin", type: "checkbox", required: false }
  ];

  const editFields = [
    { name: "name", label: "Name", type: "text", required: true },
    { name: "email", label: "Email", type: "email", required: true },
  ];

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <ProtectedRoute allowedRoles={["SUPER_ADMIN", "ADMIN"]}>
      <PageBreadcrumb pageTitle="Users" />
      <div className="mb-4">
        <Button onClick={handleCreate}>Create User</Button>
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
        title="Create User"
        fields={createFields}
        submitButtonText="Create"
      />

      <ModalForm
        isOpen={isEditModalOpen}
        onCloseAction={() => setIsEditModalOpen(false)}
        onSubmit={handleEditSubmit}
        title="Edit User"
        fields={editFields}
        initialData={selectedItem ? (selectedItem as unknown as Record<string, unknown>) : undefined}
        submitButtonText="Update"
      />

      <ConfirmModal
        isOpen={isDeleteModalOpen}
        onCloseAction={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteConfirm}
        title="Delete User"
        message={`Are you sure you want to delete the user "${selectedItem?.name}"?`}
      />
    </ProtectedRoute>
  );
}