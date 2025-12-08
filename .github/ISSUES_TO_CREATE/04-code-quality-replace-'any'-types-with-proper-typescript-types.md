# [CODE QUALITY] Replace 'any' Types with Proper TypeScript Types

**Labels**: code-quality, typescript, frontend, medium

---

## Description
Multiple instances of the `any` type are used in service files, defeating TypeScript's type safety.

## Affected Files
- `src/services/api.ts` - Lines 9, 11, 132
- `src/services/authService.ts` - Lines 46, 191

## Impact
- Loss of type safety and compile-time error checking
- No IDE autocompletion
- Runtime errors that could have been caught at compile time

## Solution
Replace `any` types with proper TypeScript types:
- Use `NodeJS.Timeout` for timeout IDs
- Create proper error type interfaces
- Use `unknown` instead of `any` when type is truly unknown

## Priority
**MEDIUM** - Improves type safety and developer experience.
