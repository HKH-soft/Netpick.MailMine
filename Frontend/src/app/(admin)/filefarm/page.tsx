import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import FileExplorer from "@/components/files/FileExplorer";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "FilePick - Documents and File Management",
  description: "Manage documents, files, and folders",
};

export default function FileFarmPage() {
  return (
    <div>
      <PageBreadcrumb pageTitle="FilePick" />
      <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-gray-900 lg:p-6">
        <FileExplorer />
      </div>
    </div>
  );
}
