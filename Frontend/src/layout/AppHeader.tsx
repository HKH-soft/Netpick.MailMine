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
      className="sticky top-0 z-40 flex w-full transition-all duration-300 bg-white/60 dark:bg-black/60 backdrop-blur-xl dark:backdrop-blur-xl border-b border-gray-200 dark:border-gray-800"
    >
      <div className="flex items-center justify-between w-full px-4 md:px-6 h-14">
        {/* Left: mobile menu + search */}
        <div className="flex items-center gap-3">
          <button
            onClick={handleToggle}
            className="flex items-center justify-center w-9 h-9 rounded-xl text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 transition-all duration-200 lg:hidden"
            aria-label="Toggle Sidebar"
          >
            {isMobileOpen ? <CloseIcon /> : <MenuToggleIcon />}
          </button>

          {/* Search bar */}
          <button
            onClick={openCommandPalette}
            className="hidden md:flex items-center gap-3 h-9 px-3.5 rounded-xl border border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-900 hover:bg-gray-100 dark:hover:bg-gray-800 hover:border-gray-300 dark:hover:border-gray-700 transition-all duration-200 group min-w-[240px]"
            data-testid="search-button"
          >
            <SearchIcon className="w-4 h-4 text-gray-400 dark:text-gray-500 group-hover:text-gray-500 dark:group-hover:text-gray-400 transition-colors" />
            <span className="text-[13px] text-gray-400 dark:text-gray-500 group-hover:text-gray-500 dark:group-hover:text-gray-400 transition-colors">Search...</span>
            <kbd className="ml-auto text-[10px] text-gray-400 dark:text-gray-500 bg-gray-100 dark:bg-gray-800 px-1.5 py-0.5 rounded border border-gray-200 dark:border-gray-700 font-mono">Ctrl K</kbd>
          </button>
        </div>

        {/* Right: actions */}
        <div className="flex items-center gap-1.5" data-testid="header-actions">
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
