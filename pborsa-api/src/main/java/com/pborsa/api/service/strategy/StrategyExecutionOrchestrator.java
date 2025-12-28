package com.pborsa.api.service.strategy;

import com.pborsa.api.client.tradingengine.TradingEngineClient;
import com.pborsa.api.config.tradingengine.TradingEngineProperties;
import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.exception.StrategyExecutionException;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.credentials.UserCredentialsService;
import io.grpc.StatusRuntimeException;
import io.temporal.client.ActivityCompletionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import org.springframework.stereotype.Service;

/**
 * Streams historical trades from Alpaca to the trading engine in batches.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyExecutionOrchestrator {

    private static final int MIN_BATCH_SIZE = 100;

    private final UserCredentialsService credentialsService;
    private final AlpacaClientFactory alpacaClientFactory;
    private final TradingEngineClient tradingEngineClient;
    private final AlpacaTradeFetcher tradeFetcher;
    private final TradeBatcher tradeBatcher;
    private final TradingEngineProperties tradingEngineProperties;

    public void execute(StrategyExecutionContext context) {
        execute(context, null);
    }

    public void execute(StrategyExecutionContext context, Runnable heartbeat) {
        log.info("Strategy execution {}: streaming {} trades for user {} strategy {} from {} to {}",
                context.executionId(), context.symbol(), context.userId(), context.strategyId(),
                context.start(), context.end());

        if (!tradingEngineClient.isEnabled()) {
            log.info("Trading engine client disabled. Skipping streaming for execution {}.", context.executionId());
            return;
        }

        AlpacaCredentialsDto credentials = credentialsService.getCredentials(context.userId());
        AlpacaAPI client = alpacaClientFactory.getOrCreateClient(credentials);

        int batchSize = Math.max(MIN_BATCH_SIZE, tradingEngineProperties.getBatchSize());
        int pageLimit = tradingEngineProperties.getPageLimit();
        String symbol = context.symbol().toUpperCase();
        Runnable heartbeatRunner = heartbeat != null ? heartbeat : () -> {};

        String nextPageToken = null;
        try (TradingEngineClient.TradingEngineStream stream = tradingEngineClient.startExecution(context)) {
            StrategyExecutionStreamGuard guard = new StrategyExecutionStreamGuard(stream, heartbeatRunner);
            guard.checkpoint();
            do {
                AlpacaTradeFetcher.TradePage page = tradeFetcher.fetchPage(
                        client,
                        context.executionId(),
                        symbol,
                        context.start(),
                        context.end(),
                        pageLimit,
                        nextPageToken,
                        guard::checkpoint
                );

                log.info("Execution {} fetched {} trades (pageToken={}, nextPageToken={})",
                        context.executionId(), page.trades().size(), nextPageToken, page.nextPageToken());

                tradeBatcher.forEachBatch(page.trades(), symbol, batchSize, guard::sendTrades);

                nextPageToken = page.nextPageToken();
            } while (hasNext(nextPageToken));
        } catch (ActivityCompletionException e) {
            log.info("Strategy execution {} cancelled", context.executionId());
            throw e;
        } catch (ApiException e) {
            log.error("Alpaca API error streaming execution {}", context.executionId(), e);
            throw new StrategyExecutionException(
                    StrategyExecutionException.ErrorCode.DATA_STREAM_ERROR,
                    "Failed to fetch historical trades from Alpaca: " + e.getMessage(),
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
