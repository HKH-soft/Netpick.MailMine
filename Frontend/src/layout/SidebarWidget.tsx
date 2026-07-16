import React from "react";
import Image from "next/image";

export default function SidebarWidget() {
  return (
    <div
      className={`
        mx-auto mb-10 w-full max-w-60 rounded-2xl bg-gray-50 px-4 py-5 text-center dark:bg-white/[0.03]`}
    >
      <Image
        src="/images/Netpick-Platform/Netpick.svg"
        alt="Netpick"
        width={40}
        height={40}
        className="mx-auto mb-2"
      />
      <h3 className="mb-2 font-semibold text-gray-900 dark:text-white">
        Netpick
      </h3>
      <p className="text-gray-500 text-theme-sm dark:text-gray-400">
        Scrape, manage, and analyze contacts and emails.
      </p>
    </div>
  );
}



