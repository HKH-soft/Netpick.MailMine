# [BUG] React Hooks Rules Violation in Scrape Control Page

**Labels**: bug, high, frontend, react

---

## Description
React Hook "useScrapeControls" is being called inside a callback, which violates the Rules of Hooks.

## Location
- **File**: `src/app/(admin)/(others-pages)/scrape/control/page.tsx`
- **Line**: 238

## Problem
React Hooks must be called at the top level of a React function component, not inside callbacks, conditions, or loops.

## Impact
- Can cause unpredictable behavior
- May cause hooks to be called in different orders on re-renders
- Can lead to state management bugs
- Application may crash in production

## Solution
Move the hook call to the top level of the component.

## Priority
**HIGH** - Can cause runtime errors and unpredictable behavior.
