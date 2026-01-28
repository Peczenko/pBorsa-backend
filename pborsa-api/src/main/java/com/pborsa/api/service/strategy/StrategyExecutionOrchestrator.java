package com.pborsa.api.service.strategy;

import com.pborsa.api.client.tradingengine.TradingEngineClient;
import com.pborsa.api.config.strategy.StrategyExecutionProperties;
import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.domain.dto.market.StockBarDto;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.exception.StrategyExecutionException;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.credentials.UserCredentialsService;
import com.pborsa.api.service.mapper.MarketDataMapper;
import io.grpc.StatusRuntimeException;
import io.temporal.client.ActivityCompletionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import org.springframework.stereotype.Service;

import java.util.function.Function;

import static com.pborsa.api.exception.StrategyExecutionException.ErrorCode.DATA_STREAM_ERROR;
import static com.pborsa.api.exception.StrategyExecutionException.ErrorCode.TRADING_ENGINE_UNAVAILABLE;

/**
 * Streams historical bar data from Alpaca to the trading engine in batches.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyExecutionOrchestrator {

    private static final int MIN_BATCH_SIZE = 100;

    private final UserCredentialsService credentialsService;
    private final AlpacaClientFactory alpacaClientFactory;
    private final TradingEngineClient tradingEngineClient;
    private final AlpacaBarFetcher barFetcher;
    private final DataBatcher dataBatcher;
    private final MarketDataMapper marketDataMapper;
    private final StrategyExecutionProperties executionProperties;

    public void execute(StrategyExecutionContext context) {
        execute(context, null);
    }

    public void execute(StrategyExecutionContext context, Runnable heartbeat) {
        log.info("Strategy execution {}: streaming {} bars ({}) for user {} strategy {} from {} to {}",
                context.executionId(), context.symbol(), executionProperties.getTimeframe(),
                context.userId(), context.strategyId(), context.start(), context.end());

        if (!tradingEngineClient.isEnabled()) {
            log.info("Trading engine client disabled. Skipping streaming for execution {}.", context.executionId());
            return;
        }

        AlpacaCredentialsDto credentials = credentialsService.getCredentials(context.userId());
        AlpacaAPI client = alpacaClientFactory.getOrCreateClient(credentials);

        int batchSize = Math.max(MIN_BATCH_SIZE, executionProperties.getBatchSize());
        int pageLimit = executionProperties.getPageLimit();
        String symbol = context.symbol().toUpperCase();
        Runnable heartbeatRunner = heartbeat != null ? heartbeat : () -> {};

        Function<StockBar, StockBarDto> barMapper = bar -> marketDataMapper.toStockBarDto(bar, symbol);

        String nextPageToken = null;
        try (TradingEngineClient.TradingEngineStream stream = tradingEngineClient.startExecution(context)) {
            StrategyExecutionStreamGuard guard = new StrategyExecutionStreamGuard(stream, heartbeatRunner);
            guard.checkpoint();
            do {
                BarPage page = barFetcher.fetchPage(
                        client,
                        context.executionId(),
                        symbol,
                        context.start(),
                        context.end(),
                        executionProperties.getTimeframe(),
                        pageLimit,
                        nextPageToken,
                        executionProperties.getStockFeed(),
                        guard::checkpoint
                );

                log.info("Execution {} fetched {} bars (pageToken={}, nextPageToken={})",
                        context.executionId(), page.bars().size(), nextPageToken, page.nextPageToken());

                dataBatcher.processBatches(page.bars(), batchSize, barMapper, guard::sendBars);

                nextPageToken = page.nextPageToken();
            } while (hasNext(nextPageToken));
        } catch (ActivityCompletionException e) {
            log.info("Strategy execution {} cancelled", context.executionId());
            throw e;
        } catch (ApiException e) {
            log.error("Alpaca API error streaming execution {}", context.executionId(), e);
            throw new StrategyExecutionException(
                    DATA_STREAM_ERROR,
                    "Failed to fetch historical bars from Alpaca: " + e.getMessage(),
                    e
            );
        } catch (StatusRuntimeException | IllegalStateException e) {
            log.error("Trading engine unavailable for execution {}", context.executionId(), e);
            throw new StrategyExecutionException(
                    TRADING_ENGINE_UNAVAILABLE,
                    "Trading engine unavailable: " + e.getMessage(),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error streaming execution {}", context.executionId(), e);
            throw new StrategyExecutionException(
                    DATA_STREAM_ERROR,
                    "Failed to stream data to trading engine",
                    e
            );
        }
    }

    private boolean hasNext(String token) {
        return token != null && !token.isBlank();
    }
}
