-- V9: Add missing description column to all BaseEntity-derived tables
-- BaseEntity defines a description column; several earlier migrations omitted it

ALTER TABLE device_sessions ADD COLUMN description TEXT;
ALTER TABLE scrape_data ADD COLUMN description TEXT;
ALTER TABLE contacts ADD COLUMN description TEXT;
ALTER TABLE notifications ADD COLUMN description TEXT;
ALTER TABLE pipeline ADD COLUMN description TEXT;
ALTER TABLE email_tag_assignments ADD COLUMN description TEXT;
ALTER TABLE campaign_recipients ADD COLUMN description TEXT;
ALTER TABLE mfa_settings ADD COLUMN description TEXT;
ALTER TABLE mfa_backup_codes ADD COLUMN description TEXT;
