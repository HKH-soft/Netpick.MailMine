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
    <div className="min-h-screen bg-black text-white font-inter">
      <AnnouncementBanner />
      <Navbar />
      <main>{children}</main>
      <Footer />
    </div>
  );
}
