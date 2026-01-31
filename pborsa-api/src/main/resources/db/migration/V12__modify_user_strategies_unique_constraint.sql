-- Modify unique constraint to include status
-- This allows users to have the same strategy for a symbol with different statuses
-- (e.g., have a STOPPED strategy and create a new CREATED one)

-- Drop the existing unique constraint
ALTER TABLE user_strategies DROP CONSTRAINT IF EXISTS uk_user_strategies_user_base_symbol;

-- Add new unique constraint including status
ALTER TABLE user_strategies
    ADD CONSTRAINT uk_user_strategies_user_base_symbol_status
    UNIQUE (user_id, base_strategy_id, symbol, status);
