-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    name VARCHAR(255),
    profile_image_key VARCHAR(255),
    role_id UUID NOT NULL REFERENCES roles(id),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP,
    verification_code VARCHAR(255),
    verification_code_expires_at TIMESTAMP,
    account_expires_at TIMESTAMP,
    verification_attempts INTEGER,
    verification_last_sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create indexes for users
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);

-- Create scrape_jobs table
CREATE TABLE IF NOT EXISTS scrape_job (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    scrape_link TEXT NOT NULL UNIQUE,
    attempt_number INTEGER NOT NULL DEFAULT 0,
    been_scraped BOOLEAN DEFAULT FALSE,
    scrape_failed BOOLEAN DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_scrapejob_link ON scrape_job(scrape_link);

-- Create scrape_data table
CREATE TABLE IF NOT EXISTS scrape_data (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    file_name VARCHAR(255) NOT NULL,
    attempt_number INTEGER NOT NULL,
    job_id UUID NOT NULL REFERENCES scrape_job(id),
    parsed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create contacts table
CREATE TABLE IF NOT EXISTS contacts (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    scrape_data_id UUID NOT NULL REFERENCES scrape_data(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create contact_emails table (for Contact entity emails collection)
CREATE TABLE IF NOT EXISTS contact_emails (
    contact_id UUID NOT NULL REFERENCES contacts(id),
    email VARCHAR(255),
    PRIMARY KEY (contact_id, email)
);

-- Create proxies table
CREATE TABLE IF NOT EXISTS proxies (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    name VARCHAR(255),
    host VARCHAR(255),
    port INTEGER,
    protocol VARCHAR(20) NOT NULL DEFAULT 'SOCKS5',
    username VARCHAR(255),
    password VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'UNTESTED',
    last_tested_at TIMESTAMP,
    last_used_at TIMESTAMP,
    success_count INTEGER DEFAULT 0,
    failure_count INTEGER DEFAULT 0,
    avg_response_time_ms INTEGER,
    uuid VARCHAR(255),
    encryption VARCHAR(50),
    transport VARCHAR(20),
    security VARCHAR(20),
    sni VARCHAR(255),
    path VARCHAR(255),
    ws_host VARCHAR(255),
    alpn VARCHAR(255),
    fingerprint VARCHAR(255),
    public_key VARCHAR(255),
    short_id VARCHAR(255),
    alter_id INTEGER,
    flow VARCHAR(100),
    original_link TEXT,
    local_port INTEGER,
    vercel_token VARCHAR(255),
    relay_session_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create api_keys table
CREATE TABLE IF NOT EXISTS api_keys (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    key VARCHAR(255) NOT NULL UNIQUE,
    point_left INTEGER,
    link_id VARCHAR(255) NOT NULL,
    search_engine_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create search_queries table
CREATE TABLE IF NOT EXISTS search_queries (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    sentence VARCHAR(255) NOT NULL UNIQUE,
    link_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_searchquery_sentence ON search_queries(sentence);

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create pipeline table
CREATE TABLE IF NOT EXISTS pipeline (
    id UUID PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    stage VARCHAR(50),
    state VARCHAR(50),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    items_processed INTEGER DEFAULT 0,
    items_total INTEGER DEFAULT 0,
    current_step_name VARCHAR(255),
    links_created INTEGER DEFAULT 0,
    pages_scraped INTEGER DEFAULT 0,
    contacts_found INTEGER DEFAULT 0,
    errors_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Insert default roles
INSERT OR IGNORE INTO roles (name, description) VALUES ('USER', 'Regular user');
INSERT OR IGNORE INTO roles (name, description) VALUES ('ADMIN', 'Administrator');
INSERT OR IGNORE INTO roles (name, description) VALUES ('SUPER_ADMIN', 'Super administrator');