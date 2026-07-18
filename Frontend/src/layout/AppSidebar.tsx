"use client";
import React, { useEffect, useRef, useState, useCallback, useMemo } from "react";
import Link from "next/link";
import Image from "next/image";
import { usePathname } from "next/navigation";
import { useSidebar } from "../context/SidebarContext";
import AuthService from "../services/authService";
import { ChevronDownIcon, GridIcon } from "../icons/index";

type NavItem = {
  name: string;
  icon: React.ReactNode;
  path?: string;
  subItems?: { name: string; path: string; pro?: boolean; new?: boolean }[];
};

const navItems: NavItem[] = [
  {
    icon: <GridIcon />,
    name: "Core",
    subItems: [
      { name: "Dashboard", path: "/dashboard", pro: false },
      { name: "Statistics", path: "/statistics", pro: false },
    ],
  },
  {
    icon: <Image src="/images/Netpick-Platform/GateKeeper.svg" alt="" width={20} height={20} />,
    name: "Gatekeeper",
    subItems: [
      { name: "User Profile", path: "/profile", pro: false },
      { name: "Users", path: "/users", pro: false },
      { name: "Settings", path: "/settings", pro: false },
    ],
  },
  {
    name: "MailMine",
    icon: <Image src="/images/Netpick-Platform/MailMine.svg" alt="" width={20} height={20} />,
    subItems: [
      { name: "Email", path: "/email", pro: false },
      { name: "Analytics", path: "/analytics", pro: false, new: true },
      { name: "Campaigns", path: "/campaigns", pro: false, new: true },
      { name: "Shared Inboxes", path: "/shared-inboxes", pro: false, new: true },
      { name: "Follow-ups", path: "/follow-ups", pro: false, new: true },
      { name: "Email Auth Check", path: "/email-auth", pro: false, new: true },
      { name: "Control", path: "/scrape/control", pro: false },
      { name: "Pipelines", path: "/scrape/pipelines", pro: false },
      { name: "Jobs", path: "/scrape/jobs", pro: false },
      { name: "Data", path: "/scrape/data", pro: false },
      { name: "Contacts", path: "/scrape/contacts", pro: false },
      { name: "Api keys", path: "/scrape/api-keys", pro: false },
      { name: "Search Queries", path: "/scrape/search-querys", pro: false },
      { name: "Query Generator", path: "/scrape/query-generator", pro: false, new: true },
      { name: "Proxies", path: "/scrape/proxies", pro: false },
      { name: "AI", path: "/scrape/ai", pro: false, new: true },
    ],
  },
  { icon: <GridIcon />, name: "DealPick", subItems: [{ name: "Deals", path: "/dealfarm", pro: false }] },
  { icon: <GridIcon />, name: "TaskPick", subItems: [{ name: "Tasks", path: "/taskfarm", pro: false }, { name: "Projects", path: "/taskfarm/projects", pro: false }] },
  { icon: <GridIcon />, name: "FilePick", subItems: [{ name: "Files", path: "/filefarm", pro: false }] },
  { icon: <GridIcon />, name: "FinancePick", subItems: [{ name: "Invoices", path: "/financefarm", pro: false }] },
  { icon: <GridIcon />, name: "InventoryPick", subItems: [{ name: "Inventory", path: "/inventoryfarm", pro: false }] },
];

const AppSidebar: React.FC = () => {
  const { isExpanded, isMobileOpen, isHovered, setIsHovered } = useSidebar();
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
      if (item.name === "Access") {
        return userRole === "SUPER_ADMIN" || userRole === "ADMIN";
      }
      return true;
    });
  }, [userRole]);

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
        <li key={nav.name}>
          {nav.subItems ? (
            <button
              onClick={() => handleSubmenuToggle(index, menuType)}
              className={`w-full flex items-center gap-3 px-3 py-2 rounded-xl text-[13px] font-medium transition-all duration-200 group
                ${openSubmenu?.type === menuType && openSubmenu?.index === index
                  ? "bg-green-500/[0.08] text-green-400"
                  : "text-white/40 hover:text-white/70 hover:bg-white/[0.04]"
                }
                ${isCollapsed ? "lg:justify-center lg:px-2" : ""}
              `}
            >
              <span className={`shrink-0 transition-colors ${
                openSubmenu?.type === menuType && openSubmenu?.index === index
                  ? "text-green-400" : "text-white/30 group-hover:text-white/50"
              }`}>
                {nav.icon}
              </span>
              {!isCollapsed && (
                <>
                  <span className="flex-1 text-left truncate">{nav.name}</span>
                  <ChevronDownIcon className={`w-4 h-4 transition-transform duration-200 shrink-0 ${
                    openSubmenu?.type === menuType && openSubmenu?.index === index ? "rotate-180 text-green-400/60" : "text-white/20"
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
                    ? "bg-green-500/[0.08] text-green-400"
                    : "text-white/40 hover:text-white/70 hover:bg-white/[0.04]"
                  }
                  ${isCollapsed ? "lg:justify-center lg:px-2" : ""}
                `}
              >
                <span className={`shrink-0 transition-colors ${
                  isActive(nav.path) ? "text-green-400" : "text-white/30 group-hover:text-white/50"
                }`}>
                  {nav.icon}
                </span>
                {!isCollapsed && <span className="truncate">{nav.name}</span>}
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
              <ul className="mt-0.5 ml-5 pl-3 space-y-0.5 border-l border-white/[0.04]">
                {nav.subItems.map((subItem) => (
                  <li key={subItem.name}>
                    <Link
                      href={subItem.path}
                      className={`flex items-center justify-between px-3 py-1.5 rounded-lg text-[12px] transition-all duration-200 ${
                        isActive(subItem.path)
                          ? "bg-green-500/[0.06] text-green-400 font-medium"
                          : "text-white/30 hover:text-white/60 hover:bg-white/[0.03]"
                      }`}
                    >
                      <span className="truncate">{subItem.name}</span>
                      <span className="flex items-center gap-1 ml-1 shrink-0">
                        {subItem.new && (
                          <span className="text-[9px] font-semibold uppercase tracking-wider px-1.5 py-0.5 rounded bg-green-500/10 text-green-400">new</span>
                        )}
                        {subItem.pro && (
                          <span className="text-[9px] font-semibold uppercase tracking-wider px-1.5 py-0.5 rounded bg-amber-500/10 text-amber-400">pro</span>
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
        className={`fixed top-0 left-0 h-screen z-50 transition-all duration-300 ease-out
          ${isMobileOpen
            ? "translate-x-0 w-[290px]"
            : isExpanded || isHovered
              ? "translate-x-0 w-[290px]"
              : "translate-x-0 w-[72px]"
          }
          lg:translate-x-0
        `}
        style={{
          background: isMobileOpen
            ? "rgba(8, 8, 8, 0.97)"
            : "rgba(8, 8, 8, 0.95)",
          borderRight: "1px solid rgba(255, 255, 255, 0.04)",
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
                <h2 className="mb-3 px-3 text-[10px] font-semibold uppercase tracking-wider text-white/20">Menu</h2>
              )}
              {renderMenuItems(filteredNavItems, "main")}
            </div>
          </nav>
        </div>

        {/* Collapse toggle */}
        <div className="absolute bottom-4 left-0 right-0 flex justify-center">
          <button
            onClick={() => setIsHovered(!isExpanded)}
            className="hidden lg:flex items-center justify-center w-8 h-8 rounded-lg bg-white/[0.04] hover:bg-white/[0.08] text-white/20 hover:text-white/50 transition-all duration-200"
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
