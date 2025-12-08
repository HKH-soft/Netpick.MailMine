# [CODE QUALITY] Remove Unused Variables and Imports

**Labels**: code-quality, cleanup, frontend, medium

---

## Description
Multiple files contain unused variables, imports, and ESLint disable directives.

## Affected Files
1. `src/app/(admin)/(others-pages)/(access)/users/page.tsx` - 3 unused variables
2. `src/app/(admin)/(others-pages)/scrape/proxies/page.tsx` - 1 unused variable
3. `src/app/(admin)/(others-pages)/statistics/page.tsx` - 1 unused import
4. `src/components/auth/ResetPasswordForm.tsx` - 1 unused variable
5. `src/components/auth/SignInForm.tsx` - 1 unused variable
6. `src/components/auth/VerifyForm.tsx` - 2 issues
7. `src/components/header/UserDropdown.tsx` - 1 unused parameter
8. `src/services/api.ts` - 1 unused variable
9. `src/services/authService.ts` - 1 unused variable

## Solution
Run `npm run lint -- --fix` and manually review items that can't be auto-fixed.

## Priority
**MEDIUM** - Improves code quality but not critical.
