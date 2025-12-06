"use client";
import React, { useState, useEffect } from "react";
import { Dropdown } from "../ui/dropdown/Dropdown";
import { DropdownItem } from "../ui/dropdown/DropdownItem";
import AuthService from "@/services/authService";
import UserService from "@/services/userService";
import { useAuth } from "@/context/AuthContext";
import { ChevronDownSmIcon, UserProfileIcon, SettingsIcon, SupportIcon, SignoutIcon } from "@/icons";

// 定义用户类型
interface User {
  name: string;
  email: string;
  profileImage?: string;
}

export default function UserDropdown() {
  const [isOpen, setIsOpen] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const { logout } = useAuth();

  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        const userProfile = await UserService.getCurrentUser();
        setUser({
          name: userProfile.name,
          email: userProfile.email,
          // profileImage: userProfile.profileImageKey
        });
      } catch (error) {
        console.error("Error fetching user profile:", error);

        // Fallback to token info if API fails
        const token = AuthService.getToken();
        if (token) {
          try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            setUser({
              name: payload.name || "User",
              email: payload.sub || "user@example.com"
            });
          } catch (e) {
            // Fallback to default
            setUser({
              name: "User",
              email: "user@example.com"
            });
          }
        }
      }
    };

    fetchUserProfile();
  }, []);

  function toggleDropdown(e: React.MouseEvent<HTMLButtonElement, MouseEvent>) {
    e.stopPropagation();
    setIsOpen((prev) => !prev);
  }

  function closeDropdown() {
    setIsOpen(false);
  }

  function handleSignOut() {
    logout(); // Use the logout function from AuthContext
    window.location.href = "/signin";
  }

  return (
    <div className="relative">
      <button
        onClick={toggleDropdown}
        className="flex items-center text-gray-700 dark:text-gray-400 dropdown-toggle"
      >
        <span className="mr-3 overflow-hidden rounded-full h-11 w-11 bg-black dark:bg-white">
        </span>

        <span className="block mr-1 font-medium text-theme-sm">{user?.name || "User"}</span>

        <ChevronDownSmIcon className={`stroke-gray-500 dark:stroke-gray-400 transition-transform duration-200 ${isOpen ? "rotate-180" : ""}`} />
      </button>

      <Dropdown
        isOpen={isOpen}
        onCloseAction={closeDropdown}
        className="absolute right-0 mt-[17px] flex w-[260px] flex-col rounded-2xl border border-gray-200 bg-white p-3 shadow-theme-lg dark:border-gray-800 dark:bg-gray-dark"
      >
        <div>
          <span className="block font-medium text-gray-700 text-theme-sm dark:text-gray-400">
            {user?.name || "User"}
          </span>
          <span className="mt-0.5 block text-theme-xs text-gray-500 dark:text-gray-400">
            {user?.email || "user@example.com"}
          </span>
        </div>

        <ul className="flex flex-col gap-1 pt-4 pb-3 border-b border-gray-200 dark:border-gray-800">
          <li>
            <DropdownItem
              onItemClick={closeDropdown}
              tag="a"
              href="/profile"
              className="flex items-center gap-3 px-3 py-2 font-medium text-gray-700 rounded-lg group text-theme-sm hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
            >
              <UserProfileIcon className="fill-gray-500 group-hover:fill-gray-700 dark:fill-gray-400 dark:group-hover:fill-gray-300" />
              Edit profile
            </DropdownItem>
          </li>
          <li>
            <DropdownItem
              onItemClick={closeDropdown}
              tag="a"
              href="/profile"
              className="flex items-center gap-3 px-3 py-2 font-medium text-gray-700 rounded-lg group text-theme-sm hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
            >
              <SettingsIcon className="fill-gray-500 group-hover:fill-gray-700 dark:fill-gray-400 dark:group-hover:fill-gray-300" />
              Account settings
            </DropdownItem>
          </li>
          <li>
            <DropdownItem
              onItemClick={closeDropdown}
              tag="a"
              href="/profile"
              className="flex items-center gap-3 px-3 py-2 font-medium text-gray-700 rounded-lg group text-theme-sm hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
            >
              <SupportIcon className="fill-gray-500 group-hover:fill-gray-700 dark:fill-gray-400 dark:group-hover:fill-gray-300" />
              Support
            </DropdownItem>
          </li>
        </ul>
        <button
          onClick={handleSignOut}
          className="flex items-center gap-3 px-3 py-2 mt-3 font-medium text-gray-700 rounded-lg group text-theme-sm hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300 w-full text-left"
        >
          <SignoutIcon className="fill-gray-500 group-hover:fill-gray-700 dark:group-hover:fill-gray-300" />
          Sign out
        </button>
      </Dropdown>
    </div>
  );
}