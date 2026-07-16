"use client";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import Switch from "@/components/form/switch/Switch";
import Button from "@/components/ui/button/Button";
import Link from "next/link";
import React from "react";

export default function SettingsPage() {
  return (
    <div>
      <PageBreadcrumb pageTitle="Settings" />
      <div className="space-y-6">
        <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] lg:p-6">
          <h3 className="mb-5 text-lg font-semibold text-gray-800 dark:text-white/90 lg:mb-7">
            App Preferences
          </h3>
          <div className="space-y-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-700 dark:text-gray-400">
                  Dark Mode
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Toggle dark mode theme
                </p>
              </div>
              <Switch label="" defaultChecked={false} onChange={() => {}} />
            </div>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-700 dark:text-gray-400">
                  Language
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Select your preferred language
                </p>
              </div>
              <select className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-700 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-300">
                <option value="en">English</option>
                <option value="fa">فارسی</option>
              </select>
            </div>
          </div>
        </div>

        <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] lg:p-6">
          <h3 className="mb-5 text-lg font-semibold text-gray-800 dark:text-white/90 lg:mb-7">
            Notification Settings
          </h3>
          <div className="space-y-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-700 dark:text-gray-400">
                  Email Notifications
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Receive email notifications for important updates
                </p>
              </div>
              <Switch label="" defaultChecked={true} onChange={() => {}} />
            </div>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-700 dark:text-gray-400">
                  Push Notifications
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Receive push notifications in your browser
                </p>
              </div>
              <Switch label="" defaultChecked={true} onChange={() => {}} />
            </div>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-700 dark:text-gray-400">
                  Marketing Emails
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Receive emails about new features and updates
                </p>
              </div>
              <Switch label="" defaultChecked={false} onChange={() => {}} />
            </div>
          </div>
        </div>

        <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] lg:p-6">
          <h3 className="mb-5 text-lg font-semibold text-gray-800 dark:text-white/90 lg:mb-7">
            Account Settings
          </h3>
          <div className="space-y-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-700 dark:text-gray-400">
                  Change Password
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Update your account password
                </p>
              </div>
              <Link href="/change-password">
                <Button size="sm" variant="outline">
                  Change
                </Button>
              </Link>
            </div>
            <div className="border-t border-gray-200 dark:border-gray-700 pt-5">
              <h4 className="mb-3 text-sm font-semibold text-red-500">
                Danger Zone
              </h4>
              <p className="mb-4 text-xs text-gray-500 dark:text-gray-400">
                Permanently delete your account and all associated data. This
                action cannot be undone.
              </p>
              <Button size="sm" variant="outline" className="text-red-500 ring-red-300 hover:bg-red-50 dark:ring-red-700 dark:hover:bg-red-500/10">
                Delete Account
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
