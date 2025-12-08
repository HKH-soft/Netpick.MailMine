#!/bin/bash
# Script to create GitHub issues from the frontend analysis
# Usage: bash create-frontend-issues.sh [repository]
# Example: bash create-frontend-issues.sh HKH-soft/Netpick.MailMine

# Note: This script requires GitHub CLI (gh) to be installed and authenticated
# Install: https://cli.github.com/
# Authenticate: gh auth login

set -euo pipefail

# Allow repository to be passed as argument, default to HKH-soft/Netpick.MailMine
REPO="${1:-HKH-soft/Netpick.MailMine}"

echo "Creating frontend issues for $REPO..."
echo ""

# Issue 1: Critical Security Vulnerabilities
echo "Creating Issue 1: Security Vulnerabilities..."
gh issue create \
  --repo "$REPO" \
  --title "[CRITICAL] Security Vulnerabilities in npm Dependencies" \
  --label "security,critical,dependencies,frontend" \
  --body "## Description
Multiple critical and moderate security vulnerabilities have been identified in npm dependencies.

## Affected Dependencies
- **Next.js 15.2.3**: 5 critical vulnerabilities (RCE, SSRF, information disclosure)
- **ESLint**: ReDoS vulnerability
- **brace-expansion**: ReDoS vulnerability
- **js-yaml**: Prototype pollution

## Solution
\`\`\`bash
cd Frontend
# First try safe fixes
npm audit fix

# For critical Next.js update (breaking changes possible):
# Test in development environment first, then run:
npm audit fix --force
# Review and test all changes before committing
\`\`\`

**Note**: The --force flag may introduce breaking changes. Test thoroughly after updating.

## Priority
**CRITICAL** - Fix immediately before production deployment.

---
Generated from automated frontend analysis"

# Issue 2: React Hooks Violation
echo "Creating Issue 2: React Hooks Violation..."
gh issue create \
  --repo "$REPO" \
  --title "[BUG] React Hooks Rules Violation in Scrape Control Page" \
  --label "bug,high,frontend,react" \
  --body "## Description
React Hook \"useScrapeControls\" is being called inside a callback, violating the Rules of Hooks.

## Location
- **File**: \`src/app/(admin)/(others-pages)/scrape/control/page.tsx\`
- **Line**: 238

## Impact
- Can cause unpredictable behavior
- State management bugs
- Potential crashes

## Solution
Move hook call to component top level.

## References
- [Rules of Hooks](https://react.dev/reference/rules/rules-of-hooks)

---
Generated from automated frontend analysis"

# Issue 3: Unused Variables
echo "Creating Issue 3: Unused Variables..."
gh issue create \
  --repo "$REPO" \
  --title "[CODE QUALITY] Remove Unused Variables and Imports" \
  --label "code-quality,cleanup,frontend" \
  --body "## Description
Multiple files contain unused variables, imports, and ESLint directives.

## Affected Files (9 files)
- \`src/app/(admin)/(others-pages)/(access)/users/page.tsx\` - 3 unused
- \`src/app/(admin)/(others-pages)/scrape/proxies/page.tsx\` - 1 unused
- \`src/app/(admin)/(others-pages)/statistics/page.tsx\` - 1 unused
- \`src/components/auth/ResetPasswordForm.tsx\` - 1 unused
- \`src/components/auth/SignInForm.tsx\` - 1 unused
- \`src/components/auth/VerifyForm.tsx\` - 2 issues
- \`src/components/header/UserDropdown.tsx\` - 1 unused
- \`src/services/api.ts\` - 1 unused
- \`src/services/authService.ts\` - 1 unused

## Solution
\`\`\`bash
cd Frontend
npm run lint -- --fix
\`\`\`
Then manually review items that can't be auto-fixed.

---
Generated from automated frontend analysis"

# Issue 4: TypeScript any usage
echo "Creating Issue 4: TypeScript any Types..."
gh issue create \
  --repo "$REPO" \
  --title "[CODE QUALITY] Replace 'any' Types with Proper TypeScript Types" \
  --label "code-quality,typescript,frontend" \
  --body "## Description
Multiple instances of \`any\` type usage defeat TypeScript's type safety.

## Affected Files
- \`src/services/api.ts\` - Lines 9, 11, 132
- \`src/services/authService.ts\` - Lines 46, 191

## Impact
- Loss of type safety
- No IDE autocompletion
- Potential runtime errors

## Solution
- Use \`NodeJS.Timeout\` for timeout IDs
- Create proper error interfaces
- Use \`unknown\` instead of \`any\`

---
Generated from automated frontend analysis"

# Issue 5: Array Index Keys
echo "Creating Issue 5: Array Index as React Keys..."
gh issue create \
  --repo "$REPO" \
  --title "[BUG] Using Array Index as Key in React Lists" \
  --label "bug,frontend,react" \
  --body "## Description
Multiple components use array index as React \`key\` prop, causing rendering issues.

## Affected Files
1. \`src/components/tables/BasicTableOne.tsx\`
2. \`src/components/form/MultiSelect.tsx\`
3. \`src/app/(admin)/(others-pages)/scrape/ai/page.tsx\`
4. \`src/app/(admin)/(others-pages)/scrape/query-generator/page.tsx\`

## Impact
- Items don't update correctly
- State attached to wrong items
- Animation issues

## Solution
Use unique IDs from data objects instead of array indices.

---
Generated from automated frontend analysis"

# Issue 6: Environment Variables Documentation
echo "Creating Issue 6: Environment Variables Documentation..."
gh issue create \
  --repo "$REPO" \
  --title "[DOCUMENTATION] Add .env.example File for Environment Variables" \
  --label "documentation,configuration,frontend" \
  --body "## Description
No \`.env.example\` file to document required environment variables.

## Current State
- \`src/app/api/[...path]/route.ts\` uses \`process.env.BACKEND_URL\`
- No documentation for new developers

## Solution
Create \`Frontend/.env.example\` with:
\`\`\`env
BACKEND_URL=http://localhost:8080
NEXT_PUBLIC_APP_NAME=MailMine
NEXT_PUBLIC_APP_VERSION=1.0.0
\`\`\`

Also update README.md with setup instructions.

---
Generated from automated frontend analysis"

# Issue 7: Password Reset
echo "Creating Issue 7: Password Reset Implementation..."
gh issue create \
  --repo "$REPO" \
  --title "[FEATURE] Complete Password Reset Implementation" \
  --label "feature,authentication,frontend,backend" \
  --body "## Description
Password reset functionality has TODO comments indicating incomplete implementation.

## Location
\`src/components/auth/ResetPasswordForm.tsx\` - Lines 43, 61, 78, 93

## Required
1. Backend API endpoints for reset flow
2. Frontend service methods in authService.ts
3. Complete integration in ResetPasswordForm

## Impact
Users cannot reset forgotten passwords - poor UX for locked-out users.

---
Generated from automated frontend analysis"

# Issue 8: React Query
echo "Creating Issue 8: React Query for Data Fetching..."
gh issue create \
  --repo "$REPO" \
  --title "[IMPROVEMENT] Implement React Query for Data Fetching" \
  --label "enhancement,frontend,performance" \
  --body "## Description
Add React Query (TanStack Query) for better data fetching and caching.

## Benefits
- Automatic request deduplication
- Background refetching
- Optimistic updates
- Better loading/error states
- Reduced boilerplate

## Implementation
\`\`\`bash
npm install @tanstack/react-query
\`\`\`

---
Generated from automated frontend analysis"

# Issue 9: Error Boundaries
echo "Creating Issue 9: Error Boundaries..."
gh issue create \
  --repo "$REPO" \
  --title "[IMPROVEMENT] Add React Error Boundaries" \
  --label "enhancement,frontend,error-handling" \
  --body "## Description
No error boundaries implemented - errors crash the entire app.

## Solution
Add error boundaries at:
- App level (catch all errors)
- Page level (isolate page errors)
- Component level (complex components)

## Benefits
- Graceful error handling
- Better user experience
- Error reporting integration

---
Generated from automated frontend analysis"

# Issue 10: Skeleton Loaders
echo "Creating Issue 10: Skeleton Loaders..."
gh issue create \
  --repo "$REPO" \
  --title "[IMPROVEMENT] Replace Loading Text with Skeleton Loaders" \
  --label "enhancement,frontend,ux" \
  --body "## Description
Simple \"Loading...\" text instead of proper skeleton loaders.

## Benefits
- Better perceived performance
- More professional appearance
- Reduces layout shift

## Affected Files
- \`src/app/(admin)/(others-pages)/scrape/jobs/page.tsx\`
- Other pages with loading states

---
Generated from automated frontend analysis"

# Issue 11: TypeScript Configuration
echo "Creating Issue 11: TypeScript Configuration..."
gh issue create \
  --repo "$REPO" \
  --title "[IMPROVEMENT] Enhance TypeScript Configuration" \
  --label "enhancement,typescript,frontend" \
  --body "## Description
Add stricter TypeScript compiler options.

## Suggested Additions to tsconfig.json
\`\`\`json
{
  \"compilerOptions\": {
    \"noUnusedLocals\": true,
    \"noUnusedParameters\": true,
    \"noImplicitReturns\": true,
    \"noFallthroughCasesInSwitch\": true
  }
}
\`\`\`

## Benefits
Catches more errors at compile time.

---
Generated from automated frontend analysis"

# Issue 12: Request Cancellation
echo "Creating Issue 12: Request Cancellation..."
gh issue create \
  --repo "$REPO" \
  --title "[IMPROVEMENT] Add Request Cancellation for API Calls" \
  --label "enhancement,frontend,performance" \
  --body "## Description
In-flight requests not cancelled on component unmount.

## Problem
Potential memory leaks from uncancelled requests.

## Solution
Use AbortController in API service and hooks.

---
Generated from automated frontend analysis"

# Issue 13: Input Debouncing
echo "Creating Issue 13: Input Debouncing..."
gh issue create \
  --repo "$REPO" \
  --title "[IMPROVEMENT] Add Input Debouncing for Search Fields" \
  --label "enhancement,frontend,performance" \
  --body "## Description
Search inputs should be debounced to reduce API calls.

## Benefits
- Reduces server load
- Improves performance
- Better user experience

---
Generated from automated frontend analysis"

# Issue 14: E2E Testing
echo "Creating Issue 14: E2E Testing..."
gh issue create \
  --repo "$REPO" \
  --title "[IMPROVEMENT] Add E2E Testing with Playwright" \
  --label "enhancement,testing,frontend" \
  --body "## Description
Add end-to-end testing for critical user flows.

## Suggested Tests
- Authentication flow
- Scrape job management
- Contact viewing
- Dashboard metrics

## Implementation
\`\`\`bash
npm install -D @playwright/test
npx playwright install
\`\`\`

---
Generated from automated frontend analysis"

# Issue 15: Accessibility ARIA
echo "Creating Issue 15: Accessibility - ARIA Labels..."
gh issue create \
  --repo "$REPO" \
  --title "[ACCESSIBILITY] Add ARIA Labels and Improve Accessibility" \
  --label "accessibility,frontend" \
  --body "## Description
Many interactive elements lack proper ARIA labels for screen readers.

## Issues
- Modal dialogs without ARIA
- Form inputs without labels
- Icon buttons without text alternatives
- Missing focus management

## Solution
Audit and add proper ARIA labels to all interactive elements.

---
Generated from automated frontend analysis"

echo ""
echo "✅ All 15 issues created successfully!"
echo ""
echo "View issues at: https://github.com/$REPO/issues"
