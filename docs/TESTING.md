# Frontend Test Suite

## Architecture Analysis

### Component Hierarchy
```
App Layout
├── I18nProviderWrapper
├── QueryProvider (TanStack Query)
├── AuthProvider (Context)
├── ThemeProvider (Context)
├── ToastProvider (Context)
├── SidebarProvider
├── ExtensionProviderWrapper
└── children
```

### State Management Patterns
1. **React Context**: Auth, Theme, Sidebar, Toast, Extensions
2. **TanStack Query**: Server state management with hooks
3. **Local Component State**: useState for UI interactions

### Data Flow
1. Services layer → API calls with mock mode support
2. Hooks → Wrap services with React state
3. Components → Consume hooks for data and UI state

## Test Coverage

### Unit Tests
- `src/utils/persianNumbers.test.ts` - Number conversion utilities (Arabic ↔ Persian)
- `src/services/api.test.ts` - HTTP client (GET/POST/PUT/DELETE, error handling, token refresh)
- `src/services/authService.test.ts` - Authentication (token storage, signin, signup, verify)
- `src/services/contactService.test.ts` - Contact CRUD operations
- `src/services/userService.test.ts` - User management operations
- `src/services/scrapeJobService.test.ts` - Scrape job operations
- `src/hooks/useUsers.test.ts` - User data fetching hook
- `src/hooks/useContacts.test.ts` - Contact data fetching hook
- `src/hooks/useDeals.test.ts` - Deal data fetching hook
- `src/hooks/useModal.test.ts` - Modal state management hook
- `src/context/SidebarContext.test.tsx` - Sidebar context state management
- `src/components/ui/modal/index.test.tsx` - Modal component behavior
- `src/components/common/ProtectedRoute.test.tsx` - Route guard component

### Integration Tests
- Auth flow with context + service integration
- Protected route validation
- Form validation (SignInForm)

### E2E Tests
- `tests/e2e/auth.spec.ts` - Signin/signup/reset flow
- `tests/e2e/dashboard.spec.ts` - Dashboard metrics and navigation

## Running Tests

```bash
# Unit tests
npm test                    # Run all tests
npm test:coverage          # Coverage report

# E2E tests
npm test:e2e               # Headless mode
npm test:e2e:ui            # UI mode
```

## Coverage Thresholds
- Lines: 80%
- Functions: 75%
- Branches: 70%
- Statements: 80%

## Services Missing Tests (43 total)
- Email services (email, emailMessage, emailTemplate, emailAuth, emailQueue, emailTag)
- Scrape services (scrape, scrapeData, searchQuery, searchQueryGenerator)
- Analytics services (analytics)
- Campaign services (campaign)
- Task services (task)
- Project services (project)
- File services (file, attachment)
- Invoice services (invoice)
- Notification services (notification)
- 33 more services...

## Hooks Missing Tests (24 total)
- useScrape, useScrapeJobs, useScrapeData, useScrapeMessages
- usePipelines, useProxies, useTasks, useProjects, useProducts
- useInvoices, useTransactions, useApiKeys, useFiles, useFolders
- useEmailMessages, useCurrentUser, useGoBack
- And 14 more...

## Components Missing Tests (50+ total)
- Form components
- Chart components  
- Table components
- Dashboard widgets
- Layout components
- And many more...