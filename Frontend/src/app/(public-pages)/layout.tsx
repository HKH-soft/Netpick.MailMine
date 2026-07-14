"use client";
import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { MoonIcon, SunIcon } from "@/icons";
import { useTheme } from "@/context/ThemeContext";

const publicNavItems = [
  { name: "Home", path: "/" },
  { name: "About Us", path: "/about" },
  { name: "Contact Us", path: "/contact" },
  { name: "Blogs", path: "/blogs" },
];

export default function PublicPageLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const { theme, toggleTheme } = useTheme();

  return (
    <div className="min-h-screen bg-white dark:bg-gray-900">
      {/* Header */}
      <header className="border-b border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Link href="/" className="text-2xl font-bold text-gray-800 dark:text-white">
                Netpick
              </Link>
            </div>
            <nav className="flex space-x-8">
              {publicNavItems.map((item) => (
                <Link
                  key={item.path}
                  href={item.path}
                  className={`text-sm font-medium transition-colors ${
                    pathname === item.path
                      ? "text-brand-500"
                      : "text-gray-600 dark:text-gray-300 hover:text-brand-500"
                  }`}
                >
                  {item.name}
                </Link>
              ))}
            </nav>
            <div className="flex items-center gap-4">
              <Link
                href="/signin"
                className="text-sm font-medium text-gray-600 dark:text-gray-300 hover:text-brand-500"
              >
                Sign In
              </Link>
              <button
                onClick={toggleTheme}
                className="p-2 rounded-lg text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800"
              >
                {theme === "dark" ? <SunIcon className="w-5 h-5" /> : <MoonIcon className="w-5 h-5" />}
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {children}
      </main>

      {/* Footer */}
      <footer className="border-t border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 py-8 mt-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center text-sm text-gray-500 dark:text-gray-400">
            © 2026 Netpick. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  );
}