// FolderItem.tsx
"use client";
import React from "react";
import { Folder } from "@/services/folderService";
import { FiFolder, FiTrash2 } from "react-icons/fi";

interface FolderItemProps {
  folder: Folder;
}

export default function FolderItem({ folder }: FolderItemProps) {
  return (
    <div className="flex flex-col items-center p-3 border border-gray-200 rounded-lg hover:bg-gray-50 group cursor-pointer">
      <FiFolder className="w-8 h-8 text-yellow-500 mb-2" />
      <span className="text-sm text-center truncate w-full" title={folder.name}>
        {folder.name}
      </span>
      <div className="flex gap-1 mt-2 opacity-0 group-hover:opacity-100 transition-opacity">
        <button
          onClick={() => {}}
          className="p-1 text-gray-500 hover:text-red-500"
          title="Delete"
        >
          <FiTrash2 className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}