package com.pborsa.api.market.service;

import com.pborsa.api.market.config.BarDataProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates bar data request parameters.
 */
@Component
@RequiredArgsConstructor
public class BarDataValidator {

    private final BarDataProperties barDataProperties;

    public void validateSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol is required");
        }
    }

    public String validateAndNormalizeTimeframe(String timeframe) {
        String normalized = timeframe != null ? timeframe : barDataProperties.getDefaultTimeframe();

        if (!barDataProperties.isTimeframeSupported(normalized)) {
            throw new IllegalArgumentException(
                    "Unsupported timeframe: " + normalized +
                            ". Supported: " + barDataProperties.getSupportedTimeframes()
            );
        }

        return normalized;
    }

    public int validateLimit(Integer limit) {
        if (limit == null) {
            return barDataProperties.getDefaultBarsLimit();
        }
        return Math.min(Math.max(limit, 1), barDataProperties.getMaxBarsPerRequest());
    }
}
