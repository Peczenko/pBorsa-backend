package com.pborsa.api.service.strategy;

import com.pborsa.api.client.tradingengine.TradingEngineClient;
import com.pborsa.api.config.tradingengine.TradingEngineProperties;
import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.domain.dto.market.StockBarDto;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.exception.StrategyExecutionException;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.credentials.UserCredentialsService;
import com.pborsa.api.service.mapper.MarketDataMapper;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockAdjustment;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBarsResp;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import net.jacobpeterson.alpaca.openapi.marketdata.model.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Streams historical bars from Alpaca to the trading engine in batches.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyExecutionOrchestrator {

    private static final int MIN_BATCH_SIZE = 100;

    private final UserCredentialsService credentialsService;
    private final AlpacaClientFactory alpacaClientFactory;
    private final TradingEngineClient tradingEngineClient;
    private final MarketDataMapper marketDataMapper;
    private final TradingEngineProperties tradingEngineProperties;

    public void execute(StrategyExecutionContext context) {
        log.info("Strategy execution {}: streaming {} bars for user {} strategy {} from {} to {} (tf={})",
                context.executionId(), context.symbol(), context.userId(), context.strategyId(),
                context.start(), context.end(), context.timeframe());

        if (!tradingEngineClient.isEnabled()) {
            log.info("Trading engine client disabled. Skipping streaming for execution {}.", context.executionId());
            return;
        }

        AlpacaCredentialsDto credentials = credentialsService.getCredentials(context.userId());
        AlpacaAPI client = alpacaClientFactory.getOrCreateClient(credentials);

        int batchSize = Math.max(MIN_BATCH_SIZE, tradingEngineProperties.getBatchSize());
        int pageLimit = tradingEngineProperties.getPageLimit();
        String symbol = context.symbol().toUpperCase();
        String timeframe = StringUtils.hasText(context.timeframe()) ? context.timeframe().trim() : "1Min";

        String nextPageToken = null;
        try (TradingEngineClient.TradingEngineStream stream = tradingEngineClient.startExecution(context)) {
            do {
                StockBarsResp resp = client.marketData().stock().stockBars(
                        symbol,
                        timeframe,
                        OffsetDateTime.ofInstant(context.start(), ZoneOffset.UTC),
                        OffsetDateTime.ofInstant(context.end(), ZoneOffset.UTC),
                        (long) pageLimit,
                        StockAdjustment.RAW,
                        null,
                        StockFeed.IEX,
                        null,
                        nextPageToken,
                        Sort.ASC
                );

                Map<String, List<StockBar>> barsMap = resp.getBars();
                List<StockBar> bars = barsMap != null
                        ? barsMap.getOrDefault(symbol, Collections.emptyList())
                        : Collections.emptyList();

                log.info("Execution {} fetched {} bars (pageToken={}, nextPageToken={})",
                        context.executionId(), bars.size(), nextPageToken, resp.getNextPageToken());

                List<StockBarDto> chunk = new ArrayList<>(batchSize);
                for (StockBar bar : bars) {
                    chunk.add(marketDataMapper.toStockBarDto(bar, symbol));
                    if (chunk.size() >= batchSize) {
                        stream.sendBatch(List.copyOf(chunk));
                        chunk.clear();
                    }
                }
                if (!chunk.isEmpty()) {
                    stream.sendBatch(List.copyOf(chunk));
                    chunk.clear();
                }

                nextPageToken = resp.getNextPageToken();
            } while (hasNext(nextPageToken));
        } catch (ApiException e) {
            log.error("Alpaca API error streaming execution {}", context.executionId(), e);
            throw new StrategyExecutionException(
                    StrategyExecutionException.ErrorCode.DATA_STREAM_ERROR,
                    "Failed to fetch historical bars from Alpaca: " + e.getMessage(),
                    e
            );
        } catch (StatusRuntimeException e) {
            log.error("Trading engine unavailable for execution {}", context.executionId(), e);
            throw new StrategyExecutionException(
                    StrategyExecutionException.ErrorCode.TRADING_ENGINE_UNAVAILABLE,
                    "Trading engine unavailable: " + e.getMessage(),
                    e
            );
        } catch (IllegalStateException e) {
            log.error("Trading engine unavailable for execution {}", context.executionId(), e);
            throw new StrategyExecutionException(
                    StrategyExecutionException.ErrorCode.TRADING_ENGINE_UNAVAILABLE,
                    "Trading engine unavailable: " + e.getMessage(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error streaming execution {}", context.executionId(), e);
            throw new StrategyExecutionException(
                    StrategyExecutionException.ErrorCode.DATA_STREAM_ERROR,
                    "Failed to stream data to trading engine",
                    e
            );
        }
    }

    private boolean hasNext(String token) {
        return token != null && !token.isBlank();
    }
}
