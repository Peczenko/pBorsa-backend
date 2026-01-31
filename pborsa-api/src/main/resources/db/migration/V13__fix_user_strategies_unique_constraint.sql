-- Fix unique constraint to only prevent duplicate ACTIVE or CREATED strategies
-- This allows multiple STOPPED, PAUSED, etc. strategies for the same user/base/symbol
-- but prevents having two ACTIVE or two CREATED strategies simultaneously

-- Drop the existing unique constraint that includes status
ALTER TABLE user_strategies DROP CONSTRAINT IF EXISTS uk_user_strategies_user_base_symbol_status;

-- Create partial unique index for ACTIVE status only
-- This prevents a user from having multiple ACTIVE strategies for the same base_strategy + symbol
CREATE UNIQUE INDEX idx_user_strategies_unique_active
    ON user_strategies (user_id, base_strategy_id, symbol)
    WHERE status = 'ACTIVE';

-- Create partial unique index for CREATED status only
-- This prevents a user from having multiple CREATED strategies for the same base_strategy + symbol
CREATE UNIQUE INDEX idx_user_strategies_unique_created
    ON user_strategies (user_id, base_strategy_id, symbol)
    WHERE status = 'CREATED';

-- Create partial unique index for PREPARING status only
-- This prevents a user from having multiple PREPARING strategies for the same base_strategy + symbol
CREATE UNIQUE INDEX idx_user_strategies_unique_preparing
    ON user_strategies (user_id, base_strategy_id, symbol)
    WHERE status = 'PREPARING';
