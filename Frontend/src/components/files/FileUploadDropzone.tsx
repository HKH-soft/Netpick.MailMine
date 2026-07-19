"use client";
import React, { useCallback, useState } from "react";
import { useDropzone } from "react-dropzone";
import { UploadIcon } from "@/icons";
import FileService from "@/services/fileService";
import { useToast } from "@/context/ToastContext";

interface FileUploadDropzoneProps {
  folderId?: string | null;
  onUploadComplete?: () => void;
}

export default function FileUploadDropzone({ folderId, onUploadComplete }: FileUploadDropzoneProps) {
  const { addToast } = useToast();
  const [uploading, setUploading] = useState(false);

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    if (acceptedFiles.length === 0) return;
    
    setUploading(true);
    try {
      for (const file of acceptedFiles) {
        await FileService.uploadFile(file, folderId ?? undefined);
      }
      addToast("success", "Upload Complete", `${acceptedFiles.length} file(s) uploaded successfully`);
      onUploadComplete?.();
    } catch {
      addToast("error", "Upload Failed", "Could not upload files");
    } finally {
      setUploading(false);
    }
  }, [folderId, onUploadComplete, addToast]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: true,
  });

  return (
    <div
      {...getRootProps()}
      className={`flex items-center justify-center w-full p-6 mb-4 border border-dashed rounded-xl transition-colors cursor-pointer
        ${isDragActive ? "border-brand-500 bg-brand-500/5" : "border-white/[0.08] hover:border-brand-500/50"}
        ${uploading ? "opacity-50 pointer-events-none" : ""}
      `}
    >
      <input {...getInputProps()} />
      <div className="text-center">
        <UploadIcon className="w-8 h-8 mx-auto mb-2 text-white/40" />
        <p className="text-sm text-white/60">
          {uploading ? "Uploading..." : isDragActive ? "Drop files here" : "Drag & drop files or click to browse"}
        </p>
      </div>
    </div>
  );
}