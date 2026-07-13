# Update AppSidebar.tsx with new extension-based navigation structure

$sidebarFile = "H:\Netpick.MailMine\Frontend\src\layout\AppSidebar.tsx"
$content = Get-Content $sidebarFile -Raw

# Replace the navItems section
$newNavItems = @'
const navItems: NavItem[] = [
  {
    icon: <GridIcon />,
    name: "Core",
    subItems: [
      { name: "Dashboard", path: "/", pro: false },
      { name: "Statistics", path: "/statistics", pro: false },
    ],
  },

  {
    icon: <UserCircleIcon />,
    name: "Gatekeeper",
    subItems: [
      { name: "User Profile", path: "/profile", pro: false },
      { name: "Users", path: "/users", pro: false },
      { name: "Settings", path: "/settings", pro: false },
    ],
  },

  {
    name: "MailMine",
    icon: <PageIcon />,
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
];
'@

# Find and replace navItems section
$pattern = 'const navItems: NavItem\[\] = \[[\s\S]*?\{[\s\S]*?icon: <SettingsIcon \/>,[\s\S]*?name: "Settings",[\s\S]*?path: "/settings",[\s\S]*?\},[\s\S]*?\];'

$content = $content -replace $pattern, $newNavItems

Set-Content -Path $sidebarFile -Value $content -Encoding UTF8
Write-Host "AppSidebar.tsx updated with extension-based navigation"