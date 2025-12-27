"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ProtectedRoute from "@/components/common/ProtectedRoute";
import DynamicTable, { ColumnConfig } from "@/components/tables/DynamicTable";
import Pagination from "@/components/tables/Pagination";
import React, { useState } from "react";
// import React, { useState, useEffect } from "react"; // Uncomment if using isSuperAdmin check
import Button from "@/components/ui/button/Button";
import ModalForm from "@/components/forms/ModalForm";
import ConfirmModal from "@/components/forms/ConfirmModal";
import { useUsers } from "@/hooks/useUsers";
// import AuthService from "@/services/authService"; // Uncomment if using isSuperAdmin check
import UserService, { User } from "@/services/userService";
import AdminService from "@/services/adminService";
import { useToast } from "@/context/ToastContext";

export default function Users() {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<User | null>(null);
  // const [isSuperAdmin, setIsSuperAdmin] = useState(false); // Uncomment if needed for role-based UI
  const { addToast } = useToast();
  const { users, loading, error, totalPages, refetch } = useUsers(currentPage);

  // Check if current user is super admin (commented out to avoid unused variable)
  // useEffect(() => {
  //   const token = AuthService.getToken();
  //   if (token) {
  //     try {
  //       const payload = JSON.parse(atob(token.split('.')[1]));
  //       setIsSuperAdmin(payload.role === 'SUPER_ADMIN');
  //     } catch (e) {
  //       console.error("Error parsing token", e);
  //       setIsSuperAdmin(false);
  //     }
  //   }
  // }, []);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleCreate = () => {
    setSelectedItem(null);
    setIsCreateModalOpen(true);
  };

  const handleEdit = (row: Record<string, unknown>) => {
    const user = users.find(u => u.id === row.id);
    if (user) {
      setSelectedItem(user);
      setIsEditModalOpen(true);
    }
  };

  const handleDelete = (row: Record<string, unknown>) => {
    const user = users.find(u => u.id === row.id);
    if (user) {
      setSelectedItem(user);
      setIsDeleteModalOpen(true);
    }
  };

  const handleCreateSubmit = async (data: Record<string, unknown>) => {
    try {
      const createData = {
        name: data.name as string,
        email: data.email as string,
        password: data.password as string
      };

      // If creating an admin, use admin endpoint
      if (data.isAdmin) {
        await AdminService.createAdmin(createData);
      } else {
        await AdminService.createUser(createData);
      }

      addToast("success", "Success", "User created successfully");
      setIsCreateModalOpen(false);
      await refetch();
    } catch (err) {
      console.error("Failed to create user:", err);
      addToast("error", "Error", "Failed to create user");
    }
  };

  const handleEditSubmit = async (data: Record<string, unknown>) => {
    if (!selectedItem) return;
    try {
      await UserService.updateUser(selectedItem.id, {
        name: data.name as string,
      });
      addToast("success", "Success", "User updated successfully");
      setIsEditModalOpen(false);
      await refetch();
    } catch (err) {
      console.error("Failed to update user:", err);
      addToast("error", "Error", "Failed to update user");
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedItem) return;
    try {
      await UserService.deleteUser(selectedItem.id);
      addToast("success", "Success", "User deleted successfully");
      setIsDeleteModalOpen(false);
      await refetch();
    } catch (err) {
      console.error("Failed to delete user:", err);
      addToast("error", "Error", "Failed to delete user");
    }
  };

  // const handleRestoreUser = async (userId: string) => {
  //   try {
  //     await UserService.restoreUser(userId);
  //     addToast("success", "Success", "User restored successfully");
  //     await refetch();
  //   } catch (err) {
  //     console.error("Failed to restore user:", err);
  //     addToast("error", "Error", "Failed to restore user");
  //   }
  // };

  // Define columns with conditional edit/delete based on user role
  const columns: ColumnConfig[] = [
    { key: "name", header: "Name", type: "text" },
    { key: "email", header: "Email", type: "text" },
    { key: "role", header: "Role", type: "text" },
    { key: "isVerified", header: "Verified", type: "text" },
    { key: "edit", header: "Edit", type: "edit" },
    { key: "delete", header: "Delete", type: "delete" },
  ];

  // Transform data to match the table structure
  const data = users.map(user => ({
    id: user.id,
    name: user.name || "-",
    email: user.email || "-",
    role: user.role || "USER",
    isVerified: user.isVerified ? "Yes" : "No",
  }));

  const createFields = [
    { name: "name", label: "Name", type: "text", required: true },
    { name: "email", label: "Email", type: "email", required: true },
    { name: "password", label: "Password", type: "password", required: true },
    { name: "isAdmin", label: "Create as Admin", type: "checkbox", required: false }
  ];

  const editFields = [
    { name: "name", label: "Name", type: "text", required: true },
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
        initialData={selectedItem ? { name: selectedItem.name } : undefined}
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