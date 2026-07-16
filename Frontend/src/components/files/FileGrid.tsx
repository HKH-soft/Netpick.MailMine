// FileGrid.tsx
"use client";
import React from "react";
import { FileEntity } from "@/services/fileService";
import { Folder } from "@/services/folderService";
import FileItem from "./FileItem";
import FolderItem from "./FolderItem";

interface FileGridProps {
  files: FileEntity[];
  folders: Folder[];
  loading: boolean;
  onDeleteFile?: (fileId: string) => void;
  onDeleteFolder?: (folderId: string) => void;
  onFolderClick?: (folderId: string) => void;
}

export default function FileGrid({ files, folders, loading, onDeleteFile, onDeleteFolder, onFolderClick }: FileGridProps) {
  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">Loading...</div>
      </div>
    );
  }

  if (!files?.length && !folders?.length) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">No files or folders found</div>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
      {folders?.map((folder) => (
        <FolderItem key={folder.id} folder={folder} onDelete={onDeleteFolder} onClick={onFolderClick} />
      ))}
      {files?.map((file) => (
        <FileItem key={file.id} file={file} onDelete={onDeleteFile} />
      ))}
    </div>
  );
}
