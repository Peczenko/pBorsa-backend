UPDATE base_strategies
SET name = 'RSI',
    description = 'Identifies price extremes by buying when an asset is technically oversold and selling when it becomes overextended.',
    updated_at = NOW()
WHERE id = 1;

UPDATE base_strategies
SET name = 'BOLLINGER_BANDS',
    description = 'Capitalizes on market stability by buying at the lower volatility boundary and selling as the price reverts to its statistical mean.',
    updated_at = NOW()
WHERE id = 2;

UPDATE base_strategies
SET name = 'MACD',
    description = 'Filters out market noise to ensure trades are only placed when fast-moving averages confirm a strong directional trend.',
    updated_at = NOW()
WHERE id = 3;

-- Insert new base strategy
-- NOTE: code is NOT NULL and UNIQUE, so you must provide it.
-- Choose a code value that matches your conventions and doesn't already exist.
INSERT INTO base_strategies (name, description, code, active, created_at, updated_at)
VALUES (
           'SMART_TREND',
           'A machine-learning powered strategy that uses LightGBM to analyze price patterns and volatility across multiple timeframes, buying only when high-conviction momentum is detected.',
           'SMART_TREND',
           true,
           NOW(),
           NOW()
       );