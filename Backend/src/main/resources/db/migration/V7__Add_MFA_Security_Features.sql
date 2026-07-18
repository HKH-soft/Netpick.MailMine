-- V7: MFA, Security Events, Device Sessions, IP Policies, Password History
-- This migration adds comprehensive security features to the Gatekeeper module.

-- 1. MFA Settings per user
CREATE TABLE mfa_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    totp_secret VARCHAR(255),
    totp_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_mfa_settings_user_id ON mfa_settings(user_id);

-- 2. MFA Backup Codes
CREATE TABLE mfa_backup_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code_hash VARCHAR(255) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_mfa_backup_codes_user_id ON mfa_backup_codes(user_id);

-- 3. Security Events (immutable audit log)
CREATE TABLE security_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(50) NOT NULL,
    user_id UUID,
    user_email VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_fingerprint VARCHAR(255),
    geo_location VARCHAR(255),
    details JSONB,
    risk_score INTEGER DEFAULT 0,
    blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_security_events_type ON security_events(event_type);
CREATE INDEX idx_security_events_user_id ON security_events(user_id);
CREATE INDEX idx_security_events_ip ON security_events(ip_address);
CREATE INDEX idx_security_events_created ON security_events(created_at);
CREATE INDEX idx_security_events_risk ON security_events(risk_score);

-- 4. Device Sessions (active session tracking)
CREATE TABLE device_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_id UUID REFERENCES refresh_tokens(id) ON DELETE SET NULL,
    device_fingerprint VARCHAR(255),
    device_info TEXT,
    ip_address VARCHAR(45),
    geo_location VARCHAR(255),
    last_active_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_device_sessions_user_id ON device_sessions(user_id);
CREATE INDEX idx_device_sessions_fingerprint ON device_sessions(device_fingerprint);
CREATE INDEX idx_device_sessions_active ON device_sessions(user_id, is_revoked) WHERE deleted = FALSE;

-- 5. IP Access Policies (allowlist/blocklist)
CREATE TABLE ip_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_name VARCHAR(100) NOT NULL,
    policy_type VARCHAR(20) NOT NULL CHECK (policy_type IN ('ALLOWLIST', 'BLOCKLIST')),
    ip_address VARCHAR(45),
    ip_range_start VARCHAR(45),
    ip_range_end VARCHAR(45),
    cidr_notation VARCHAR(50),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP,
    created_by_id UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_ip_policies_type_active ON ip_policies(policy_type, is_active);

-- 6. Password History (prevent reuse)
CREATE TABLE password_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_history_user_id ON password_history(user_id);

-- 7. Add mfa_enabled flag to users for quick lookup
ALTER TABLE users ADD COLUMN mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE;

-- 8. Add last_known_ip to users for anomaly detection
ALTER TABLE users ADD COLUMN last_known_ip VARCHAR(45);
