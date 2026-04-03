package com.pborsa.api.market.service;

import com.pborsa.api.market.config.BarDataProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Calculates time ranges for bar data requests, accounting for
 * non-trading hours (weekends, market closed periods).
 */
@Component
@Slf4j
public class BarTimeRangeCalculator {

    private static final ZoneId US_EASTERN = ZoneId.of("America/New_York");
    private static final int MARKET_OPEN_HOUR = 9;
    private static final int MARKET_OPEN_MINUTE = 30;
    private static final int MARKET_CLOSE_HOUR = 16;

    private final BarDataProperties barDataProperties;

    public BarTimeRangeCalculator(BarDataProperties barDataProperties) {
        this.barDataProperties = barDataProperties;
    }

    /**
     * Calculates the appropriate time range for fetching bars.
     * When start is not provided, calculates it based on limit and timeframe
     * to ensure we get the most recent bars.
     */
    public TimeRange calculateTimeRangeForBars(Instant start, Instant end, String timeframe, int limit) {
        Instant now = Instant.now();
        Instant effectiveEnd = (end != null) ? end : now;

        Instant effectiveStart;
        if (start != null) {
            effectiveStart = start;
        } else {
            effectiveStart = calculateStartForBarsWithBuffer(effectiveEnd, timeframe, limit);
        }

        // Validate max lookback
        Duration lookback = Duration.between(effectiveStart, effectiveEnd);
        if (lookback.compareTo(barDataProperties.getMaxLookback()) > 0) {
            effectiveStart = effectiveEnd.minus(barDataProperties.getMaxLookback());
            log.warn("Lookback period exceeded maximum. Adjusted start to: {}", effectiveStart);
        }

        if (effectiveStart.isAfter(effectiveEnd)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        return new TimeRange(effectiveStart, effectiveEnd);
    }

    /**
     * Calculates start time for a given number of bars without buffer adjustments.
     */
    public Instant calculateStartForTimeframe(Instant end, String timeframe, int barCount) {
        Duration barDuration = parseTimeframeDuration(timeframe);
        return end.minus(barDuration.multipliedBy(barCount));
    }

    /**
     * Logs diagnostic information about why a time range might have no data.
     */
    public void logTradingHoursDiagnostics(Instant start, Instant end) {
        ZonedDateTime startET = start.atZone(US_EASTERN);
        ZonedDateTime endET = end.atZone(US_EASTERN);

        log.debug("Trading hours diagnostics - Start: {} ({} ET, {}), End: {} ({} ET, {})",
                start, startET.toLocalDateTime(), startET.getDayOfWeek(),
                end, endET.toLocalDateTime(), endET.getDayOfWeek());

        if (isWeekend(startET) && isWeekend(endET)) {
            log.warn("Both start and end times fall on weekends - no market data available");
        }

        if (!isWeekend(endET) && isBeforeMarketOpen(endET)) {
            log.warn("End time {} ET is before market open (9:30 AM ET) - no regular session data for that day",
                    endET.toLocalTime());
        }

        if (!isWeekend(startET) && isAfterMarketClose(startET)) {
            log.warn("Start time {} ET is after market close (4:00 PM ET)",
                    startET.toLocalTime());
        }
    }

    /**
     * Calculates start time for the requested number of bars, accounting for
     * non-trading hours (weekends, market closed periods).
     */
    private Instant calculateStartForBarsWithBuffer(Instant end, String timeframe, int barCount) {
        Duration barDuration = parseTimeframeDuration(timeframe);
        Duration totalBarDuration = barDuration.multipliedBy(barCount);

        if (timeframe.endsWith("Min") || timeframe.endsWith("Hour")) {
            double bufferMultiplier = 6.0;
            long bufferedMillis = (long) (totalBarDuration.toMillis() * bufferMultiplier);
            return end.minus(Duration.ofMillis(bufferedMillis));
        } else {
            double bufferMultiplier = 1.5;
            long bufferedMillis = (long) (totalBarDuration.toMillis() * bufferMultiplier);
            return end.minus(Duration.ofMillis(bufferedMillis));
        }
    }

    public Duration parseTimeframeDuration(String timeframe) {
        Objects.requireNonNull(timeframe, "timeframe");

        String normalized = timeframe.trim();

        if (normalized.endsWith("Min")) {
            int minutes = Integer.parseInt(normalized.replace("Min", ""));
            return Duration.ofMinutes(minutes);
        } else if (normalized.endsWith("Hour")) {
            int hours = Integer.parseInt(normalized.replace("Hour", ""));
            return Duration.ofHours(hours);
        } else if (normalized.endsWith("Day")) {
            int days = Integer.parseInt(normalized.replace("Day", ""));
            return Duration.ofDays(days);
        } else if (normalized.endsWith("Week")) {
            int weeks = Integer.parseInt(normalized.replace("Week", ""));
            return Duration.ofDays(weeks * 7L);
        }

        return Duration.ofMinutes(1);
    }

    private boolean isWeekend(ZonedDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private boolean isBeforeMarketOpen(ZonedDateTime dateTime) {
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        return hour < MARKET_OPEN_HOUR || (hour == MARKET_OPEN_HOUR && minute < MARKET_OPEN_MINUTE);
    }

    private boolean isAfterMarketClose(ZonedDateTime dateTime) {
        return dateTime.getHour() >= MARKET_CLOSE_HOUR;
    }

    /**
     * Validated time range.
     */
    public record TimeRange(Instant start, Instant end) {}
}
