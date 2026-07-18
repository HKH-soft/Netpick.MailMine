"use client";
import { ThemeToggleButton } from "@/components/common/ThemeToggleButton";
import NotificationDropdown from "@/components/header/NotificationDropdown";
import UserDropdown from "@/components/header/UserDropdown";
import LanguageSwitcher from "@/components/common/LanguageSwitcher";
import { useSidebar } from "@/context/SidebarContext";
import React from "react";
import { MenuToggleIcon, CloseIcon, SearchIcon } from "@/icons";

const AppHeader: React.FC = () => {
  const { isMobileOpen, toggleSidebar, toggleMobileSidebar } = useSidebar();

  const handleToggle = () => {
    if (window.innerWidth >= 1024) {
      toggleSidebar();
    } else {
      toggleMobileSidebar();
    }
  };

  const openCommandPalette = () => {
    window.dispatchEvent(new KeyboardEvent("keydown", { key: "k", ctrlKey: true }));
  };

  return (
    <header
      className="sticky top-0 z-40 flex w-full transition-all duration-300"
      style={{
        background: "rgba(0, 0, 0, 0.6)",
        backdropFilter: "blur(16px) saturate(180%)",
        WebkitBackdropFilter: "blur(16px) saturate(180%)",
        borderBottom: "1px solid rgba(255, 255, 255, 0.04)",
      }}
    >
      <div className="flex items-center justify-between w-full px-4 md:px-6 h-14">
        {/* Left: mobile menu + search */}
        <div className="flex items-center gap-3">
          <button
            onClick={handleToggle}
            className="flex items-center justify-center w-9 h-9 rounded-xl text-white/40 hover:text-white/70 hover:bg-white/[0.05] transition-all duration-200 lg:hidden"
            aria-label="Toggle Sidebar"
          >
            {isMobileOpen ? <CloseIcon /> : <MenuToggleIcon />}
          </button>

          {/* Search bar */}
          <button
            onClick={openCommandPalette}
            className="hidden md:flex items-center gap-3 h-9 px-3.5 rounded-xl border border-white/[0.06] bg-white/[0.03] hover:bg-white/[0.05] hover:border-white/[0.1] transition-all duration-200 group min-w-[240px]"
          >
            <SearchIcon className="w-4 h-4 text-white/25 group-hover:text-white/40 transition-colors" />
            <span className="text-[13px] text-white/25 group-hover:text-white/35 transition-colors">Search...</span>
            <kbd className="ml-auto text-[10px] text-white/15 bg-white/[0.04] px-1.5 py-0.5 rounded border border-white/[0.06] font-mono">Ctrl K</kbd>
          </button>
        </div>

        {/* Right: actions */}
        <div className="flex items-center gap-1.5">
          <ThemeToggleButton />
          <LanguageSwitcher />
          <NotificationDropdown />
          <UserDropdown />
        </div>
      </div>
    </header>
  );
};

export default AppHeader;
