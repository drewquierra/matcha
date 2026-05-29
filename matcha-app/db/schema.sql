-- =============================================================================
-- Matcha Tracker — Database Schema
-- =============================================================================
-- Run against a MySQL 8.x server:
--   mysql -u root -p < db/schema.sql
--
-- Or source interactively:
--   mysql> SOURCE /path/to/db/schema.sql;
-- =============================================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS matcha_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE matcha_db;

-- =============================================================================
-- TABLE: users
-- Stores application credentials. Passwords are hashed via BCrypt (work=10).
-- =============================================================================
CREATE TABLE IF NOT EXISTS users (
    id            INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_username (username)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE: matcha_logs
-- Tracks daily matcha cup counts per user.
-- The unique key on (user_id, log_date) enables the INSERT … ON DUPLICATE KEY
-- UPDATE upsert pattern used by DashboardController.handleLogDrinks().
-- =============================================================================
CREATE TABLE IF NOT EXISTS matcha_logs (
    id          INT  NOT NULL AUTO_INCREMENT,
    user_id     INT  NOT NULL,
    drink_count INT  NOT NULL DEFAULT 0,
    log_date    DATE NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_user_date (user_id, log_date),
    CONSTRAINT fk_matcha_logs_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- SEED DATA
-- =============================================================================
-- Seed user: drewquierra / matchamama
--
-- The password_hash below is a pre-generated BCrypt hash (work factor 10)
-- representing the plaintext password: matchamama
--
-- If login fails (hash format mismatch across BCrypt versions), regenerate it:
--
--   mvn compile exec:java \
--       -Dexec.mainClass=com.matcha.util.GenerateHash \
--       -Dexec.args="matchamama"
--
-- Then paste the printed hash into the INSERT below and re-run this script.
-- =============================================================================

INSERT IGNORE INTO users (username, password_hash)
VALUES (
    'drewquierra',
    '$2a$10$eImiTXuWVxfM37uY4JANjQLAPE3nMCXiWRH0mHDAhvAFRmT1OZFcm'
);
