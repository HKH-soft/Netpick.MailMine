// ConfirmModal.tsx
"use client";
import React from "react";
import { Modal } from "../ui/modal";
import Button from "../ui/button/Button";

interface ConfirmModalProps {
  isOpen: boolean;
  onCloseAction: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmButtonText?: string;
  cancelButtonText?: string;
}

const ConfirmModal: React.FC<ConfirmModalProps> = ({
  isOpen,
  onCloseAction,
  onConfirm,
  title,
  message,
  confirmButtonText = "Delete",
  cancelButtonText = "Cancel"
}) => {
  const handleConfirm = () => {
    onConfirm();
    onCloseAction();
  };

  return (
    <Modal isOpen={isOpen} onCloseAction={onCloseAction} className="max-w-[400px] p-6">
      <div className="text-center">
        <h3 className="mb-2 text-xl font-semibold text-gray-800 dark:text-white/90">
          {title}
        </h3>
        <p className="mb-6 text-gray-500 dark:text-gray-400">
          {message}
        </p>
        <div className="flex justify-center gap-3">
          <Button
            variant="outline"
            onClick={onCloseAction}
            className="px-4 py-2"
          >
            {cancelButtonText}
          </Button>
          <Button
            variant="primary"
            onClick={handleConfirm}
            className="px-4 py-2 bg-red-500 hover:bg-red-600"
          >
            {confirmButtonText}
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default ConfirmModal;