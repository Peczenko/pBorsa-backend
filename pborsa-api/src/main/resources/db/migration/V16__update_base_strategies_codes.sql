UPDATE base_strategies
SET code = 'RSI',
    updated_at = NOW()
WHERE id = 1;

UPDATE base_strategies
SET code = 'BOLLINGER_BANDS',
    name = 'Bollinger Bands',
    updated_at = NOW()
WHERE id = 2;

UPDATE base_strategies
SET code = 'MACD',
    updated_at = NOW()
WHERE id = 3;

UPDATE base_strategies
SET name = 'Smart Trend',
    updated_at = NOW()
WHERE id = 4;