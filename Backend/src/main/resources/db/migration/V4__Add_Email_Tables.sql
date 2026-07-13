-- Create email_tags table
CREATE TABLE IF NOT EXISTS email_tags (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    color_hex VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_email_tags_name ON email_tags(name);
CREATE INDEX IF NOT EXISTS idx_email_tags_category ON email_tags(category);

-- Create email_messages table
CREATE TABLE IF NOT EXISTS email_messages (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    message_id VARCHAR(255) NOT NULL UNIQUE,
    thread_id VARCHAR(255),
    sender_email VARCHAR(255) NOT NULL,
    sender_name VARCHAR(255),
    subject TEXT,
    body_text TEXT,
    body_html TEXT,
    received_at TIMESTAMP NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_answered BOOLEAN NOT NULL DEFAULT FALSE,
    is_flagged BOOLEAN NOT NULL DEFAULT FALSE,
    has_attachments BOOLEAN NOT NULL DEFAULT FALSE,
    mailbox_folder VARCHAR(100),
    assigned_to_id UUID REFERENCES users(id),
    assigned_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'INBOX',
    last_reply_at TIMESTAMP,
    reply_due_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_email_messages_message_id ON email_messages(message_id);
CREATE INDEX IF NOT EXISTS idx_email_messages_sender ON email_messages(sender_email);
CREATE INDEX IF NOT EXISTS idx_email_messages_received ON email_messages(received_at);
CREATE INDEX IF NOT EXISTS idx_email_messages_thread ON email_messages(thread_id);

-- Create email_recipients table (for storing recipients of each email)
CREATE TABLE IF NOT EXISTS email_recipients (
    email_message_id UUID NOT NULL REFERENCES email_messages(id) ON DELETE CASCADE,
    recipient_email VARCHAR(255) NOT NULL
);

-- Create email_tag_assignments table
CREATE TABLE IF NOT EXISTS email_tag_assignments (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    email_message_id UUID NOT NULL REFERENCES email_messages(id) ON DELETE CASCADE,
    email_tag_id UUID NOT NULL REFERENCES email_tags(id),
    confidence_score DOUBLE PRECISION,
    is_ai_generated BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_tag_assignments_email ON email_tag_assignments(email_message_id);
CREATE INDEX IF NOT EXISTS idx_tag_assignments_tag ON email_tag_assignments(email_tag_id);

-- Create email_rules table
CREATE TABLE IF NOT EXISTS email_rules (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    condition_type VARCHAR(50) NOT NULL,
    condition_value VARCHAR(255) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_value VARCHAR(255),
    priority INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_id UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_email_rules_active ON email_rules(is_active);
CREATE INDEX IF NOT EXISTS idx_email_rules_priority ON email_rules(priority);

-- Create email_templates table
CREATE TABLE IF NOT EXISTS email_templates (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    subject_template TEXT NOT NULL,
    body_template TEXT NOT NULL,
    is_shared BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_id UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_email_templates_name ON email_templates(name);
CREATE INDEX IF NOT EXISTS idx_email_templates_category ON email_templates(category);

-- Create email_queue_items table
CREATE TABLE IF NOT EXISTS email_queue_items (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    recipient VARCHAR(255),
    subject VARCHAR(255),
    body TEXT,
    attachment VARCHAR(255),
    template_name VARCHAR(255),
    created_by_user_id UUID REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    sent_at TIMESTAMP,
    priority VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_email_queue_status ON email_queue_items(status);
CREATE INDEX IF NOT EXISTS idx_email_queue_priority ON email_queue_items(priority);
CREATE INDEX IF NOT EXISTS idx_email_queue_created ON email_queue_items(created_at);

-- Create shared_inboxes table
CREATE TABLE IF NOT EXISTS shared_inboxes (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    email_address VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_shared_inboxes_email ON shared_inboxes(email_address);