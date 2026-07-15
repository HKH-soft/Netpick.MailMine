// FileExplorer.tsx
"use client";
import React, { useState } from "react";
import { useFolders } from "@/hooks/useFolders";
import { useFiles } from "@/hooks/useFiles";
import FileGrid from "./FileGrid";
import FolderService from "@/services/folderService";
import { useToast } from "@/context/ToastContext";

export default function FileExplorer() {
  const [currentFolder, setCurrentFolder] = useState<string | null>(null);
  const { folders, loading: foldersLoading, refetch: refetchFolders } = useFolders(1);
  const { files, loading: filesLoading } = useFiles(1, currentFolder || undefined);
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
      
      <FileGrid files={files} folders={folders} loading={filesLoading || foldersLoading} />
    </div>
  );
}