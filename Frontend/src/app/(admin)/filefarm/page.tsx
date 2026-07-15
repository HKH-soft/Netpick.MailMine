import type { Metadata } from "next";
import React from "react";

export const metadata: Metadata = {
  title: "FilePick - Documents and File Management",
  description: "Manage documents, files, and folders",
};

export default function FileFarmPage() {
  return (
    <div className="grid grid-cols-12 gap-4 md:gap-6">
      <div className="col-span-12">
        <h1 className="text-2xl font-bold">FilePick - Documents and File Management</h1>
        <p className="text-gray-500 dark:text-gray-400 mt-2">
          Manage documents, files, and folders
        </p>
      </div>
    </div>
  );
}