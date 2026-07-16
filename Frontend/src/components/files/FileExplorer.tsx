// FileExplorer.tsx
"use client";
import React, { useState } from "react";
import { useFolders } from "@/hooks/useFolders";
import { useFiles } from "@/hooks/useFiles";
import FileGrid from "./FileGrid";
import FolderService from "@/services/folderService";
import FileService from "@/services/fileService";
import { useToast } from "@/context/ToastContext";

export default function FileExplorer() {
  const [currentFolder, setCurrentFolder] = useState<string | null>(null);
  const { folders, loading: foldersLoading, refetch: refetchFolders } = useFolders(1);
  const { files, loading: filesLoading, refetch: refetchFiles } = useFiles(1, currentFolder || undefined);
  const { addToast } = useToast();

  const handleFolderClick = (folderId: string | null) => {
    setCurrentFolder(folderId);
  };

  const handleCreateFolder = async () => {
    const name = prompt("Enter folder name:");
    if (name) {
      try {
        await FolderService.createFolder({ name, parentId: currentFolder ?? undefined });
        addToast("success", "Success", "Folder created successfully");
        refetchFolders();
      } catch {
        addToast("error", "Error", "Failed to create folder");
      }
    }
  };

  const handleDeleteFile = async (fileId: string) => {
    if (!confirm("Delete this file?")) return;
    try {
      await FileService.deleteFile(fileId);
      addToast("success", "Success", "File deleted successfully");
      refetchFiles();
    } catch {
      addToast("error", "Error", "Failed to delete file");
    }
  };

  const handleDeleteFolder = async (folderId: string) => {
    if (!confirm("Delete this folder and all its contents?")) return;
    try {
      await FolderService.deleteFolder(folderId);
      addToast("success", "Success", "Folder deleted successfully");
      refetchFolders();
      refetchFiles();
    } catch {
      addToast("error", "Error", "Failed to delete folder");
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-sm text-gray-500">
          <span
            className="cursor-pointer hover:text-brand-500"
            onClick={() => handleFolderClick(null)}
          >
            Root
          </span>
          {currentFolder && (
            <>
              <span>/</span>
              <span className="text-gray-800">Current Folder</span>
            </>
          )}
        </div>
        <button
          onClick={handleCreateFolder}
          className="px-3 py-1 text-sm bg-brand-500 text-white rounded hover:bg-brand-600"
        >
          New Folder
        </button>
      </div>

      <FileGrid
        files={files}
        folders={folders}
        loading={filesLoading || foldersLoading}
        onDeleteFile={handleDeleteFile}
        onDeleteFolder={handleDeleteFolder}
        onFolderClick={handleFolderClick}
      />
    </div>
  );
}
