-- V6: Audit trail + GDPR retention configuration

CREATE TABLE IF NOT EXISTS audit_trail (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by_id UUID REFERENCES users(id),
    performed_by_email VARCHAR(255),
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_trail(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_trail(performed_by_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_trail(created_at);

-- GDPR retention configuration table
CREATE TABLE IF NOT EXISTS gdpr_retention_config (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    entity_type VARCHAR(100) NOT NULL UNIQUE,
    retention_days INTEGER NOT NULL DEFAULT 365,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_cleanup_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Default retention configs (7 years for emails, 1 year for audit, 90 days for drafts)
INSERT OR IGNORE INTO gdpr_retention_config (id, entity_type, retention_days, is_active) VALUES
    (lower(hex(randomblob(16))), 'EMAIL_MESSAGE', 2555, TRUE),
    (lower(hex(randomblob(16))), 'AUDIT_TRAIL', 365, TRUE),
    (lower(hex(randomblob(16))), 'CAMPAIGN', 730, TRUE),
    (lower(hex(randomblob(16))), 'EMAIL_QUEUE_ITEM', 90, TRUE);
