-- Cleanup strategy test data (uses high IDs to avoid conflicts)
DELETE FROM user_strategies WHERE id >= 9000;
DELETE FROM users WHERE id >= 9000;
DELETE FROM base_strategies WHERE id >= 9000;
