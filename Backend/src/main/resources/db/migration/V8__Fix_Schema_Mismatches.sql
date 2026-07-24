-- V8: Fix schema mismatches between JPA entities and Flyway migrations
--
-- Issues fixed:
-- 1. BaseEntity.description column missing from users, email_messages, email_queue_items
-- 2. Campaign table named "campaigns" in V5 but entity expects "email_campaigns"
-- 3. email_queue_recipients join table missing (EmailQueueItem @ElementCollection)
-- 4. users.preferences column missing (User entity Map<PreferencesEnum, String>)

-- 1. Add description column (from BaseEntity) to all tables missing it
ALTER TABLE users ADD COLUMN description TEXT;
ALTER TABLE email_messages ADD COLUMN description TEXT;
ALTER TABLE email_queue_items ADD COLUMN description TEXT;

-- 2. Rename campaigns -> email_campaigns to match @Table(name = "email_campaigns")
ALTER TABLE campaigns RENAME TO email_campaigns;

-- Also update the FK reference in campaign_recipients if needed
-- (SQLite doesn't track FK names in ALTER TABLE, but the FK constraint is by column, not name)

-- 3. Create missing email_queue_recipients join table for EmailQueueItem @ElementCollection
CREATE TABLE IF NOT EXISTS email_queue_recipients (
    email_queue_item_id UUID NOT NULL REFERENCES email_queue_items(id) ON DELETE CASCADE,
    recipient_email VARCHAR(255)
);

-- 4. Add preferences column to users table
ALTER TABLE users ADD COLUMN preferences TEXT;
