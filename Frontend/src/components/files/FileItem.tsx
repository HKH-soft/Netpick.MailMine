// FileItem.tsx
"use client";
import React from "react";
import { FileEntity } from "@/services/fileService";
import { FiFile, FiDownload, FiTrash2 } from "react-icons/fi";

interface FileItemProps {
  file: FileEntity;
  onDelete?: (fileId: string) => void;
}

export default function FileItem({ file, onDelete }: FileItemProps) {
  const getFileIcon = (mimeType: string) => {
    if (mimeType?.startsWith("image/")) return "🖼️";
    if (mimeType?.includes("pdf")) return "📄";
    if (mimeType?.includes("text")) return "📝";
    return <FiFile className="w-8 h-8 text-gray-400" />;
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  return (
    <div className="flex flex-col items-center p-3 border border-gray-200 rounded-lg hover:bg-gray-50 group">
      <div className="text-4xl mb-2">{getFileIcon(file.mimeType)}</div>
      <span className="text-sm text-center truncate w-full" title={file.originalFileName}>
        {file.originalFileName}
      </span>
      <span className="text-xs text-gray-500">{formatFileSize(file.fileSize ?? 0)}</span>
      <div className="flex gap-1 mt-2 opacity-0 group-hover:opacity-100 transition-opacity">
        <button
          onClick={() => window.open(file.filePath, "_blank")}
          className="p-1 text-gray-500 hover:text-brand-500"
          title="Download"
        >
          <FiDownload className="w-4 h-4" />
        </button>
        {onDelete && (
          <button
            onClick={(e) => { e.stopPropagation(); onDelete(file.id); }}
            className="p-1 text-gray-500 hover:text-red-500"
            title="Delete"
          >
            <FiTrash2 className="w-4 h-4" />
          </button>
        )}
      </div>
    </div>
  );
}
