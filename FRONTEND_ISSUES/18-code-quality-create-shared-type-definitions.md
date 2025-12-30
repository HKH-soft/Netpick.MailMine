# [CODE QUALITY] Create Shared Type Definitions

**Labels**: code-quality, typescript, frontend, low

---

## Description
Many types are duplicated across services and components.

## Solution
- Create `src/types/` directory with shared types
- Extract common types (User, ScrapeJob, Contact, etc.)
- Import types from centralized location

## Benefits
- Single source of truth for types
- Easier to maintain
- Prevents type drift

## Priority
**LOW** - Code organization improvement.
