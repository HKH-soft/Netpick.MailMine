"use client";
import React, { useEffect, useRef, useState, useCallback, useMemo } from "react";
import Link from "next/link";
import Image from "next/image";
import { usePathname } from "next/navigation";
import { useTranslation } from "react-i18next";
import { useSidebar } from "../context/SidebarContext";
import { useExtensions, ExtensionId } from "../context/ExtensionContext";
import AuthService from "../services/authService";
import { ChevronDownIcon, GridIcon, SettingsIcon } from "../icons/index";

type NavItem = {
  nameKey: string;
  icon: React.ReactNode;
  path?: string;
  extensionId?: string;
  subItems?: { nameKey: string; path: string; pro?: boolean; new?: boolean }[];
};

const navItems: NavItem[] = [
  {
    icon: <GridIcon />,
    nameKey: "navigation.core",
    subItems: [
      { nameKey: "navigation.dashboard", path: "/dashboard", pro: false },
      { nameKey: "navigation.statistics", path: "/statistics", pro: false },
    ],
  },
  {
    icon: <Image src="/images/Netpick-Platform/GateKeeper.svg" alt="" width={20} height={20} />,
    nameKey: "navigation.gatekeeper",
    extensionId: "gatekeeper",
    subItems: [
      { nameKey: "navigation.userProfile", path: "/profile", pro: false },
      { nameKey: "navigation.users", path: "/users", pro: false },
    ],
  },
  {
    nameKey: "navigation.mailMine",
    icon: <Image src="/images/Netpick-Platform/MailMine.svg" alt="" width={20} height={20} />,
    extensionId: "mailmine",
    subItems: [
      { nameKey: "navigation.email", path: "/email", pro: false },
      { nameKey: "navigation.analytics", path: "/analytics", pro: false, new: true },
      { nameKey: "navigation.campaigns", path: "/campaigns", pro: false, new: true },
      { nameKey: "navigation.sharedInboxes", path: "/shared-inboxes", pro: false, new: true },
      { nameKey: "navigation.followUps", path: "/follow-ups", pro: false, new: true },
      { nameKey: "navigation.emailAuthCheck", path: "/email-auth", pro: false, new: true },
      { nameKey: "navigation.control", path: "/scrape/control", pro: false },
      { nameKey: "navigation.pipelines", path: "/scrape/pipelines", pro: false },
      { nameKey: "navigation.jobs", path: "/scrape/jobs", pro: false },
      { nameKey: "navigation.data", path: "/scrape/data", pro: false },
      { nameKey: "navigation.contacts", path: "/scrape/contacts", pro: false },
      { nameKey: "navigation.apiKeys", path: "/scrape/api-keys", pro: false },
      { nameKey: "navigation.searchQueries", path: "/scrape/search-querys", pro: false },
      { nameKey: "navigation.queryGenerator", path: "/scrape/query-generator", pro: false, new: true },
      { nameKey: "navigation.proxies", path: "/scrape/proxies", pro: false },
      { nameKey: "navigation.ai", path: "/scrape/ai", pro: false, new: true },
    ],
  },
  { icon: <Image src="/images/Netpick-Platform/DealPick.svg" alt="" width={20} height={20} />, nameKey: "navigation.dealPick", extensionId: "dealfarm", subItems: [{ nameKey: "navigation.deals", path: "/dealfarm", pro: false }] },
  { icon: <Image src="/images/Netpick-Platform/TaskPick.svg" alt="" width={20} height={20} />, nameKey: "navigation.taskPick", extensionId: "taskfarm", subItems: [{ nameKey: "navigation.tasks", path: "/taskfarm", pro: false }, { nameKey: "navigation.projects", path: "/taskfarm/projects", pro: false }] },
  { icon: <Image src="/images/Netpick-Platform/FilePick.svg" alt="" width={20} height={20} />, nameKey: "navigation.filePick", extensionId: "filefarm", subItems: [{ nameKey: "navigation.files", path: "/filefarm", pro: false }] },
  { icon: <Image src="/images/Netpick-Platform/FinancePick.svg" alt="" width={20} height={20} />, nameKey: "navigation.financePick", extensionId: "financefarm", subItems: [{ nameKey: "navigation.invoices", path: "/financefarm", pro: false }] },
  { icon: <Image src="/images/Netpick-Platform/InventoryPick.svg" alt="" width={20} height={20} />, nameKey: "navigation.inventoryPick", extensionId: "inventoryfarm", subItems: [{ nameKey: "navigation.inventory", path: "/inventoryfarm", pro: false }] },
  {
    icon: <SettingsIcon />,
    nameKey: "navigation.settings",
    path: "/settings",
  },
];

const AppSidebar: React.FC = () => {
  const { t } = useTranslation('common');
  const { isExpanded, isMobileOpen, isHovered, setIsHovered } = useSidebar();
  const { isExtensionEnabled } = useExtensions();
  const pathname = usePathname();
  const [userRole, setUserRole] = useState<string | null>(null);
  const [openSubmenu, setOpenSubmenu] = useState<{ type: "main" | "others"; index: number } | null>(null);
  const [subMenuHeight, setSubMenuHeight] = useState<Record<string, number>>({});
  const subMenuRefs = useRef<Record<string, HTMLDivElement | null>>({});

  useEffect(() => {
    const token = AuthService.getToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.scopes && Array.isArray(payload.scopes) && payload.scopes.length > 0) {
          setUserRole(payload.scopes[0]);
        } else {
          setUserRole(payload.role || null);
        }
      } catch (e) { console.error("Error parsing token", e); }
    }
  }, []);

  const filteredNavItems = useMemo(() => {
    return navItems.filter(item => {
      if (t(item.nameKey) === "Access") {
        return userRole === "SUPER_ADMIN" || userRole === "ADMIN";
      }
      if (item.extensionId && !isExtensionEnabled(item.extensionId as ExtensionId)) {
        return false;
      }
      return true;
    });
  }, [userRole, isExtensionEnabled, t]);

  const isActive = useCallback((path: string) => path === pathname, [pathname]);

  useEffect(() => {
    let submenuMatched = false;
    ["main"].forEach((menuType) => {
      filteredNavItems.forEach((nav, index) => {
        if (nav.subItems) {
          nav.subItems.forEach((subItem) => {
            if (isActive(subItem.path)) {
              setOpenSubmenu({ type: menuType as "main" | "others", index });
              submenuMatched = true;
            }
          });
        }
      });
    });
    if (!submenuMatched) setOpenSubmenu(null);
  }, [pathname, isActive, filteredNavItems]);

  useEffect(() => {
    if (openSubmenu !== null) {
      const key = `${openSubmenu.type}-${openSubmenu.index}`;
      if (subMenuRefs.current[key]) {
        setSubMenuHeight(prev => ({ ...prev, [key]: subMenuRefs.current[key]?.scrollHeight || 0 }));
      }
    }
  }, [openSubmenu]);

  const handleSubmenuToggle = (index: number, menuType: "main" | "others") => {
    setOpenSubmenu(prev => {
      if (prev && prev.type === menuType && prev.index === index) return null;
      return { type: menuType, index };
    });
  };

  const isCollapsed = !isExpanded && !isHovered && !isMobileOpen;

  const renderMenuItems = (items: NavItem[], menuType: "main" | "others") => (
    <ul className="flex flex-col gap-0.5">
      {items.map((nav, index) => (
        <li key={nav.nameKey}>
          {nav.subItems ? (
            <button
              onClick={() => handleSubmenuToggle(index, menuType)}
              className={`w-full flex items-center gap-3 px-3 py-2 rounded-xl text-[13px] font-medium transition-all duration-200 group
                ${openSubmenu?.type === menuType && openSubmenu?.index === index
                  ? "bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400"
                  : "text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800"
                }
                ${isCollapsed ? "lg:justify-center lg:px-2" : ""}
              `}
            >
              <span className={`shrink-0 transition-colors ${
                openSubmenu?.type === menuType && openSubmenu?.index === index
                  ? "text-green-500 dark:text-green-400" : "text-gray-400 dark:text-gray-500 group-hover:text-gray-600 dark:group-hover:text-gray-400"
              }`}>
                {nav.icon}
              </span>
              {!isCollapsed && (
                <>
                  <span className="flex-1 text-left truncate">{t(nav.nameKey)}</span>
                  <ChevronDownIcon className={`w-4 h-4 transition-transform duration-200 shrink-0 ${
                    openSubmenu?.type === menuType && openSubmenu?.index === index ? "rotate-180 text-green-500/60" : "text-gray-400 dark:text-gray-500"
                  }`} />
                </>
              )}
            </button>
          ) : (
            nav.path && (
              <Link
                href={nav.path}
                className={`flex items-center gap-3 px-3 py-2 rounded-xl text-[13px] font-medium transition-all duration-200 group
                  ${isActive(nav.path)
                    ? "bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400"
                    : "text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800"
                  }
                  ${isCollapsed ? "lg:justify-center lg:px-2" : ""}
                `}
              >
                <span className={`shrink-0 transition-colors ${
                  isActive(nav.path) ? "text-green-500 dark:text-green-400" : "text-gray-400 dark:text-gray-500 group-hover:text-gray-600 dark:group-hover:text-gray-400"
                }`}>
                  {nav.icon}
                </span>
                {!isCollapsed && <span className="truncate">{t(nav.nameKey)}</span>}
              </Link>
            )
          )}
          {nav.subItems && !isCollapsed && (
            <div
              ref={(el) => { subMenuRefs.current[`${menuType}-${index}`] = el; }}
              className="overflow-hidden transition-all duration-300"
              style={{
                height: openSubmenu?.type === menuType && openSubmenu?.index === index
                  ? `${subMenuHeight[`${menuType}-${index}`]}px` : "0px",
              }}
            >
<ul className="mt-0.5 ml-5 pl-3 space-y-0.5 border-l border-gray-200 dark:border-gray-800">
                 {nav.subItems.map((subItem) => (
                   <li key={subItem.nameKey}>
                     <Link
                       href={subItem.path}
                       className={`flex items-center justify-between px-3 py-1.5 rounded-lg text-[12px] transition-all duration-200 ${
                         isActive(subItem.path)
                           ? "bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400 font-medium"
                           : "text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800"
                       }`}
                     >
                       <span className="truncate">{t(subItem.nameKey)}</span>
<span className="flex items-center gap-1 ml-1 shrink-0">
                         {subItem.new && (
                           <span className="text-[9px] font-semibold uppercase tracking-wider px-1.5 py-0.5 rounded bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400">{t('common.new')}</span>
                         )}
                         {subItem.pro && (
                           <span className="text-[9px] font-semibold uppercase tracking-wider px-1.5 py-0.5 rounded bg-amber-50 dark:bg-amber-900/20 text-amber-600 dark:text-amber-400">{t('common.pro')}</span>
                         )}
                       </span>
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </li>
      ))}
    </ul>
  );

  return (
    <>
      {/* Mobile backdrop */}
      {isMobileOpen && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-40 lg:hidden" onClick={() => setIsHovered(false)} />
      )}

      <aside
        className={`fixed top-0 h-screen z-50 transition-all duration-300 ease-out app-sidebar
          ${isMobileOpen
            ? "translate-x-0 w-[290px]"
            : isExpanded || isHovered
            ? "translate-x-0 w-[290px]"
            : "translate-x-0 w-[72px]"
          }
          lg:translate-x-0 ltr:left-0 rtl:right-0
        `}
        style={{
          background: isMobileOpen
            ? "rgba(0, 0, 0, 0.97)"
            : "rgba(0, 0, 0, 0.95)",
          borderRight: "1px solid rgba(0, 0, 0, 0.08)",
          backdropFilter: "blur(20px) saturate(180%)",
          WebkitBackdropFilter: "blur(20px) saturate(180%)",
        }}
        onMouseEnter={() => !isExpanded && setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
      >
        {/* Logo */}
        <div className={`flex items-center h-16 px-4 ${isCollapsed ? "justify-center" : "justify-start gap-2.5"}`}>
          <Link href="/" className="flex items-center gap-2.5 shrink-0">
            <Image src="/images/Netpick-Platform/Netpick.svg" alt="Netpick" width={28} height={28} />
            {!isCollapsed && (
              <span className="text-[15px] font-bold text-white/90 tracking-tight">Netpick</span>
            )}
          </Link>
        </div>

        {/* Nav */}
        <div className="flex flex-col overflow-y-auto duration-300 no-scrollbar px-3 pb-6">
          <nav>
            <div className="mb-1">
              {!isCollapsed && (
                <h2 className="mb-3 px-3 text-[10px] font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500">{t('navigation.menu')}</h2>
              )}
              {renderMenuItems(filteredNavItems, "main")}
            </div>
          </nav>
        </div>

        {/* Collapse toggle */}
        <div className="absolute bottom-4 left-0 right-0 flex justify-center">
          <button
            onClick={() => setIsHovered(!isExpanded)}
            className="hidden lg:flex items-center justify-center w-8 h-8 rounded-lg bg-gray-900 dark:bg-gray-800 hover:bg-gray-800 dark:hover:bg-gray-700 text-gray-400 dark:text-gray-500 hover:text-gray-300 dark:hover:text-gray-400 transition-all duration-200"
          >
            <svg className={`w-4 h-4 transition-transform duration-300 ${isExpanded ? "rotate-0" : "rotate-180"}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
            </svg>
          </button>
        </div>
      </aside>
    </>
  );
};

export default AppSidebar;
