-- Test data for user-related tests
-- Uses high IDs (9000+) to avoid conflicts with production migrations

-- Insert test users
INSERT INTO users (id, firebase_uid, email, display_name, provider, status, role, created_at, updated_at)
VALUES
    (9101, 'user-test-uid-1', 'user-test-1@example.com', 'Test User One', 'google.com', 'ACTIVE', 'USER', NOW(), NOW()),
    (9102, 'user-test-uid-2', 'user-test-2@example.com', 'Test User Two', 'password', 'ACTIVE', 'USER', NOW(), NOW()),
    (9103, 'admin-test-uid', 'admin-test@example.com', 'Test Admin User', 'google.com', 'ACTIVE', 'ADMIN', NOW(), NOW()),
    (9104, 'suspended-test-uid', 'suspended-test@example.com', 'Suspended User', 'password', 'SUSPENDED', 'USER', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
