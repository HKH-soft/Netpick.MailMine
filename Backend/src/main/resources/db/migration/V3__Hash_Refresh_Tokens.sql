-- This migration is no longer needed - V2 already creates token_hash column
-- Kept for backward compatibility with existing PostgreSQL databases
-- SQLite: ALTER COLUMN RENAME not supported in older versions, skip if table already correct

-- Check if column needs renaming (PostgreSQL compatibility)
-- SQLite: This migration is a no-op since V2 creates token_hash directly