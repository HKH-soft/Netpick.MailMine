"use client";
import { ThemeToggleButton } from "@/components/common/ThemeToggleButton";
import NotificationDropdown from "@/components/header/NotificationDropdown";
import UserDropdown from "@/components/header/UserDropdown";
import LanguageSwitcher from "@/components/common/LanguageSwitcher";
import { useSidebar } from "@/context/SidebarContext";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import React, { useState, useEffect, useRef } from "react";
import { MenuToggleIcon, CloseIcon, DotsHorizontalIcon, SearchIcon } from "@/icons";
import { useTranslation } from "react-i18next";

interface SearchResult {
  title: string;
  path: string;
  category: string;
}

const searchablePages: SearchResult[] = [
  { title: "navigation.dashboard", path: "/", category: "General" },
  { title: "navigation.calendar", path: "/calendar", category: "General" },
  { title: "navigation.userProfile", path: "/profile", category: "General" },
  { title: "navigation.settings", path: "/settings", category: "General" },
  { title: "navigation.control", path: "/scrape/control", category: "Scraping" },
  { title: "navigation.jobs", path: "/scrape/jobs", category: "Scraping" },
  { title: "navigation.data", path: "/scrape/data", category: "Scraping" },
  { title: "navigation.contacts", path: "/scrape/contacts", category: "Scraping" },
  { title: "navigation.apiKeys", path: "/scrape/api-keys", category: "Configuration" },
  { title: "navigation.proxies", path: "/scrape/proxies", category: "Configuration" },
  { title: "navigation.pipelines", path: "/scrape/pipelines", category: "Scraping" },
  { title: "navigation.searchQueries", path: "/scrape/search-querys", category: "Scraping" },
  { title: "navigation.queryGenerator", path: "/scrape/query-generator", category: "Tools" },
  { title: "navigation.emailScraper", path: "/scrape/email", category: "Tools" },
  { title: "navigation.ai", path: "/scrape/ai", category: "Tools" },
  { title: "navigation.messages", path: "/scrape/messages", category: "General" },
  { title: "navigation.email", path: "/email", category: "Email" },
  { title: "navigation.analytics", path: "/analytics", category: "Email" },
  { title: "navigation.campaigns", path: "/campaigns", category: "Email" },
  { title: "navigation.sharedInboxes", path: "/shared-inboxes", category: "Email" },
  { title: "navigation.followUps", path: "/follow-ups", category: "Email" },
  { title: "navigation.emailAuthCheck", path: "/email-auth", category: "Email" },
];

const AppHeader: React.FC = () => {
  const [isApplicationMenuOpen, setApplicationMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [debouncedQuery, setDebouncedQuery] = useState("");
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
  const [showResults, setShowResults] = useState(false);
  const router = useRouter();
  const searchContainerRef = useRef<HTMLDivElement>(null);
  const { t } = useTranslation('common');

  const { isMobileOpen, toggleSidebar, toggleMobileSidebar } = useSidebar();

  const handleToggle = () => {
    if (window.innerWidth >= 1024) {
      toggleSidebar();
    } else {
      toggleMobileSidebar();
    }
  };

  const toggleApplicationMenu = () => {
    setApplicationMenuOpen(!isApplicationMenuOpen);
  };
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if ((event.metaKey || event.ctrlKey) && event.key === "k") {
        event.preventDefault();
        inputRef.current?.focus();
      }
    };

    const handleClickOutside = (event: MouseEvent) => {
      if (searchContainerRef.current && !searchContainerRef.current.contains(event.target as Node)) {
        setShowResults(false);
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    document.addEventListener("mousedown", handleClickOutside);

    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    const query = e.target.value;
    setSearchQuery(query);
  };

  // Debounce the search query to avoid filtering on every keystroke
  useEffect(() => {
    const id = setTimeout(() => setDebouncedQuery(searchQuery), 200);
    return () => clearTimeout(id);
  }, [searchQuery]);

  useEffect(() => {
    const query = debouncedQuery.trim();

    if (query === "") {
      setSearchResults([]);
      setShowResults(false);
      return;
    }

    const filtered = searchablePages.filter((page) =>
      t(page.title).toLowerCase().includes(query.toLowerCase()) ||
      t(`search.${page.category.toLowerCase()}`).toLowerCase().includes(query.toLowerCase())
    );
    setSearchResults(filtered);
    setShowResults(true);
  }, [debouncedQuery, t]);

  const handleResultClick = (path: string) => {
    router.push(path);
    setSearchQuery("");
    setShowResults(false);
  };

  return (
    <header className="sticky top-0 flex w-full bg-white border-gray-200 z-99999 dark:border-gray-800 dark:bg-gray-900 lg:border-b">
      <div className="flex flex-col items-center justify-between grow lg:flex-row lg:px-6">
        <div className="flex items-center justify-between w-full gap-2 px-3 py-3 border-b border-gray-200 dark:border-gray-800 sm:gap-4 lg:justify-normal lg:border-b-0 lg:px-0 lg:py-4">
          <button
            className="items-center justify-center w-10 h-10 text-gray-500 border-gray-200 rounded-lg z-99999 dark:border-gray-800 lg:flex dark:text-gray-400 lg:h-11 lg:w-11 lg:border"
            onClick={handleToggle}
            aria-label="Toggle Sidebar"
          >
            {isMobileOpen ? (
              <CloseIcon />
            ) : (
              <MenuToggleIcon />
            )}
            {/* Cross Icon */}
          </button>

          <Link href="/" className="lg:hidden">
            <Image
              width={154}
              height={32}
              className="dark:hidden"
              src="/images/Netpick-Platform/Netpick.svg"
              alt="Netpick"
            />
            <Image
              width={154}
              height={32}
              className="hidden dark:block"
              src="/images/Netpick-Platform/Netpick.svg"
              alt="Netpick"
            />
          </Link>

          <button
            onClick={toggleApplicationMenu}
            className="flex items-center justify-center w-10 h-10 text-gray-700 rounded-lg z-99999 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800 lg:hidden"
          >
            <DotsHorizontalIcon />
          </button>

          <div className="hidden lg:block">
            <form onSubmit={(e) => e.preventDefault()}>
              <div className="relative" ref={searchContainerRef}>
                <span className="absolute -translate-y-1/2 left-4 top-1/2 pointer-events-none rtl:left-auto rtl:right-4">
                  <SearchIcon className="fill-gray-500 dark:fill-gray-400" />
                </span>
                <input
                  ref={inputRef}
                  type="text"
                  value={searchQuery}
                  onChange={handleSearch}
                  onFocus={() => {
                    if (searchQuery.trim() !== "") setShowResults(true);
                  }}
                  placeholder={t('search.placeholder')}
                  className="h-11 w-full rounded-lg border border-gray-200 bg-transparent py-2.5 pl-12 pr-14 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-800 dark:bg-white/[0.03] dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800 xl:w-[430px] rtl:pr-14 rtl:pl-12"
                />

                <button
                  type="button"
                  tabIndex={-1}
                  aria-hidden="true"
                  className="absolute right-2.5 top-1/2 inline-flex -translate-y-1/2 items-center gap-0.5 rounded-lg border border-gray-200 bg-gray-50 px-[7px] py-[4.5px] text-xs -tracking-[0.2px] text-gray-500 dark:border-gray-800 dark:bg-white/[0.03] dark:text-gray-400 rtl:right-auto rtl:left-2.5"
                >
                  <span> {t('search.shortcutCtrl')} </span>
                  <span> {t('search.shortcutK')} </span>
                </button>

                {/* Search Results Dropdown */}
                {showResults && searchResults.length > 0 && (
                  <div className="absolute top-full left-0 w-full mt-2 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 max-h-96 overflow-y-auto z-50">
                    {searchResults.map((result) => (
                      <button
                        key={result.path}
                        onClick={() => handleResultClick(result.path)}
                        className="w-full text-left px-4 py-3 hover:bg-gray-50 dark:hover:bg-gray-700/50 flex items-center justify-between group transition-colors rtl:text-right"
                      >
                        <div>
                          <div className="text-sm font-medium text-gray-900 dark:text-white">
                            {t(result.title)}
                          </div>
                          <div className="text-xs text-gray-500 dark:text-gray-400">
                            {t(`search.${result.category.toLowerCase()}`)}
                          </div>
                        </div>
                        <span className="text-xs text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity">
                          {t('search.jumpTo')}
                        </span>
                      </button>
                    ))}
                  </div>
                )}

                {showResults && searchResults.length === 0 && searchQuery.trim() !== "" && (
                  <div className="absolute top-full left-0 w-full mt-2 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 p-4 text-center text-sm text-gray-500 dark:text-gray-400 z-50">
                    {t('search.noResults', { query: searchQuery })}
                  </div>
                )}
              </div>
            </form>
          </div>
        </div>
        <div
          className={`${isApplicationMenuOpen ? "flex" : "hidden"
            } items-center justify-between w-full gap-4 px-5 py-4 lg:flex shadow-theme-md lg:justify-end lg:px-0 lg:shadow-none`}
        >
          <div className="flex items-center gap-2 2xsm:gap-3">
            {/* <!-- Dark Mode Toggler --> */}
            <ThemeToggleButton />
            {/* <!-- Dark Mode Toggler --> */}

            <LanguageSwitcher />
            {/* <!-- Language Switcher --> */}

            <NotificationDropdown />
            {/* <!-- Notification Menu Area --> */}
          </div>
          {/* <!-- User Area --> */}
          <UserDropdown />

        </div>
      </div>
    </header>
  );
};

export default AppHeader;



