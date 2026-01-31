-- Cleanup user test data (uses high IDs to avoid conflicts)
DELETE FROM user_api_credentials WHERE user_id >= 9100;
DELETE FROM user_strategies WHERE user_id >= 9100;
DELETE FROM users WHERE id >= 9100;
