-- MySQL Initialization Script
-- This script runs when the MySQL container is first created

-- Create database if not exists (already created by environment variable)
-- CREATE DATABASE IF NOT EXISTS security_db;

-- Use the database
USE security_db;

-- Create products table (JPA will handle this, but we can pre-create)
-- JPA/Hibernate will auto-create based on @Entity annotations

-- Grant permissions
-- GRANT ALL PRIVILEGES ON security_db.* TO 'appuser'@'%';
-- FLUSH PRIVILEGES;

