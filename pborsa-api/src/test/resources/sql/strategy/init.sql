-- Test data for strategy-related tests
-- Uses high IDs (9000+) to avoid conflicts with production migrations

-- Insert test base strategies
INSERT INTO base_strategies (id, code, name, description, active, created_at, updated_at)
VALUES
    (9001, 'TEST_MOMENTUM', 'Test Momentum Strategy', 'Test strategy for unit tests', true, NOW(), NOW()),
    (9002, 'TEST_INACTIVE', 'Test Inactive Strategy', 'Inactive test strategy', false, NOW(), NOW()),
    (9003, 'TEST_MEAN_REVERSION', 'Test Mean Reversion', 'Another active test strategy', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Insert test user for strategy tests
INSERT INTO users (id, firebase_uid, email, display_name, provider, status, role, created_at, updated_at)
VALUES
    (9001, 'strategy-test-uid', 'strategy-test@example.com', 'Strategy Test User', 'test', 'ACTIVE', 'USER', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Insert test user strategies
INSERT INTO user_strategies (id, user_id, base_strategy_id, name, symbol, status, budget, created_at, updated_at)
VALUES
    (9001, 9001, 9001, 'My Test Strategy', 'AAPL', 'ACTIVE', 10000.00, NOW(), NOW()),
    (9002, 9001, 9003, 'My Second Strategy', 'MSFT', 'PAUSED', 5000.00, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
