# [BUG] Using Array Index as Key in React Lists

**Labels**: bug, medium, frontend, react

---

## Description
Multiple components use array index as the `key` prop in React lists, which can cause rendering issues.

## Affected Files
1. `src/components/tables/BasicTableOne.tsx`
2. `src/components/form/MultiSelect.tsx`
3. `src/app/(admin)/(others-pages)/scrape/ai/page.tsx`
4. `src/app/(admin)/(others-pages)/scrape/query-generator/page.tsx`

## Issues with Index as Key
- Items may not update correctly when list changes
- Form inputs may retain values from wrong items
- Animations and transitions may behave incorrectly

## Solution
Use unique IDs from data objects instead of array indices.

## Priority
**MEDIUM** - Can cause subtle bugs in dynamic lists.
