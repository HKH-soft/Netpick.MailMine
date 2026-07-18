"use client";

import React from "react";
import Navbar from "@/components/landing/Navbar";
import Footer from "@/components/landing/Footer";
import AnnouncementBanner from "@/components/landing/AnnouncementBanner";

export default function PublicPageLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="relative min-h-screen bg-black text-white font-inter overflow-hidden">
      <div className="noise-overlay fixed inset-0 pointer-events-none" style={{ zIndex: 0 }} />
      <div className="relative" style={{ zIndex: 1 }}>
        <AnnouncementBanner />
        <Navbar />
        <main>{children}</main>
        <Footer />
      </div>
    </div>
  );
}
