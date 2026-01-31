-- Full cleanup for tests that need a clean slate
-- Note: Uses CASCADE to handle foreign key constraints

TRUNCATE TABLE order_history CASCADE;
TRUNCATE TABLE orders CASCADE;
TRUNCATE TABLE strategy_positions CASCADE;
TRUNCATE TABLE user_strategies CASCADE;
TRUNCATE TABLE user_api_credentials CASCADE;
TRUNCATE TABLE users CASCADE;

-- Note: Don't truncate base_strategies as they come from migrations
-- Only delete test entries with high IDs
DELETE FROM base_strategies WHERE id >= 9000;
