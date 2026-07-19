"use client";

import { useSidebar } from "@/context/SidebarContext";
import AppHeader from "@/layout/AppHeader";
import AppSidebar from "@/layout/AppSidebar";
import Backdrop from "@/layout/Backdrop";
import React, { useEffect, useState } from "react";
import ProtectedRoute from "@/components/common/ProtectedRoute";

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { isExpanded, isHovered, isMobileOpen } = useSidebar();
  const [isRTL, setIsRTL] = useState(false);

  useEffect(() => {
    const updateDirection = () => {
      setIsRTL(document.documentElement.dir === 'rtl');
    };
    updateDirection();
    const observer = new MutationObserver(updateDirection);
    observer.observe(document.documentElement, { attributes: true, attributeFilter: ['dir'] });
    return () => observer.disconnect();
  }, []);

  const sidebarWidth = isMobileOpen ? 0 : (isExpanded || isHovered ? 290 : 72);

  return (
    <ProtectedRoute>
      <div className="min-h-screen xl:flex">
        <AppSidebar />
        <Backdrop />
        <div
          className="flex-1 transition-all duration-300 ease-in-out"
          style={{
            ...(isRTL ? { marginRight: sidebarWidth } : { marginLeft: sidebarWidth }),
          }}
        >
          <AppHeader />
          <div className="p-4 mx-auto max-w-(--breakpoint-2xl) md:p-6">{children}</div>
        </div>
      </div>
    </ProtectedRoute>
  );
}
