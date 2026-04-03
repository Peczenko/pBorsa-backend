package com.pborsa.domain.dto.strategy;

import com.pborsa.domain.dto.trading.OrderSide;

import java.math.BigDecimal;
import java.time.Instant;

public record FilledOrdersResponse(
        String symbol,
        BigDecimal quantity,
        OrderSide side,
        Instant filledAt
) {}
