package com.pborsa.api.service.market;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.domain.dto.market.StockQuoteDto;
import com.pborsa.api.domain.dto.market.StockTradeDto;
import com.pborsa.api.exception.AlpacaException;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.credentials.UserCredentialsService;
import com.pborsa.api.service.mapper.MarketDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.quote.StockQuoteMessage;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.trade.StockTradeMessage;
import net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataListenerAdapter;

/**
 * Service for real-time market data streaming.
 * Connects to Alpaca's WebSocket for live quote and trade updates.
 * Uses IEX exchange for free tier real-time data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeMarketDataService {

    private final AlpacaClientFactory clientFactory;
    private final UserCredentialsService credentialsService;
    private final MarketDataMapper marketDataMapper;

    // Track active subscriptions per user
    private final Map<String, Set<String>> userSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Consumer<StockQuoteDto>> quoteConsumers = new ConcurrentHashMap<>();
    private final Map<String, Consumer<StockTradeDto>> tradeConsumers = new ConcurrentHashMap<>();
    // Track streaming instances per user
    private final Map<String, net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataWebsocketInterface> streamInstances = new ConcurrentHashMap<>();

    /**
     * Subscribes to real-time quotes for symbols.
     *
     * @param userId        User ID
     * @param symbols       Stock symbols to subscribe to
     * @param quoteConsumer Callback for quote updates
     */
    public void subscribeToQuotes(String userId, Set<String> symbols,
                                  Consumer<StockQuoteDto> quoteConsumer) {
        log.info("Subscribing to quotes for symbols {} for user: {}", symbols, userId);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Store the consumer for this user
            quoteConsumers.put(userId, quoteConsumer);
            
            // Track subscriptions
            userSubscriptions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                    .addAll(symbols);

            // Get or create streaming instance
            net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataWebsocketInterface streaming = 
                    streamInstances.computeIfAbsent(userId, k -> {
                        net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataWebsocketInterface stream = 
                                client.stockMarketDataStream();
                        
                        // Set up listener that handles both quotes and trades
                        stream.setListener(new StockMarketDataListenerAdapter() {
                            @Override
                            public void onQuote(StockQuoteMessage quoteMessage) {
                                Consumer<StockQuoteDto> consumer = quoteConsumers.get(userId);
                                if (consumer != null && quoteMessage != null) {
                                    // Convert message directly to DTO
                                    StockQuoteDto dto = StockQuoteDto.builder()
                                            .symbol(quoteMessage.getSymbol())
                                            .askPrice(quoteMessage.getAskPrice() != null ? new java.math.BigDecimal(quoteMessage.getAskPrice()) : null)
                                            .askSize(quoteMessage.getAskSize() != null ? new java.math.BigDecimal(quoteMessage.getAskSize()) : null)
                                            .bidPrice(quoteMessage.getBidPrice() != null ? new java.math.BigDecimal(quoteMessage.getBidPrice()) : null)
                                            .bidSize(quoteMessage.getBidSize() != null ? new java.math.BigDecimal(quoteMessage.getBidSize()) : null)
                                            .timestamp(quoteMessage.getTimestamp() != null ? quoteMessage.getTimestamp().toInstant() : null)
                                            .build();
                                    consumer.accept(dto);
                                }
                            }
                            
                            @Override
                            public void onTrade(StockTradeMessage tradeMessage) {
                                Consumer<StockTradeDto> consumer = tradeConsumers.get(userId);
                                if (consumer != null && tradeMessage != null) {
                                    // Convert message directly to DTO
                                    StockTradeDto dto = StockTradeDto.builder()
                                            .symbol(tradeMessage.getSymbol())
                                            .price(tradeMessage.getPrice() != null ? new java.math.BigDecimal(tradeMessage.getPrice()) : null)
                                            .size(tradeMessage.getSize() != null ? new java.math.BigDecimal(tradeMessage.getSize()) : null)
                                            .exchange(tradeMessage.getExchange())
                                            .timestamp(tradeMessage.getTimestamp() != null ? tradeMessage.getTimestamp().toInstant() : null)
                                            .build();
                                    consumer.accept(dto);
                                }
                            }
                        });
                        
                        // Connect if not already connected
                        if (!stream.isConnected()) {
                            stream.connect();
                        }
                        
                        return stream;
                    });
            
            // Subscribe to quotes (add to existing subscriptions)
            Set<String> currentQuotes = streaming.getQuoteSubscriptions();
            if (currentQuotes == null) {
                currentQuotes = new HashSet<>();
            }
            Set<String> newQuotes = new HashSet<>(currentQuotes);
            newQuotes.addAll(symbols);
            streaming.setQuoteSubscriptions(newQuotes);

            log.info("Successfully subscribed to quotes for {} symbols for user: {}",
                    symbols.size(), userId);
        } catch (Exception e) {
            log.error("Failed to subscribe to quotes for user: {}", userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.STREAMING_ERROR,
                    "Failed to subscribe to quotes: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Subscribes to real-time trades for symbols.
     *
     * @param userId        User ID
     * @param symbols       Stock symbols to subscribe to
     * @param tradeConsumer Callback for trade updates
     */
    public void subscribeToTrades(String userId, Set<String> symbols,
                                  Consumer<StockTradeDto> tradeConsumer) {
        log.info("Subscribing to trades for symbols {} for user: {}", symbols, userId);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Store the consumer for this user
            tradeConsumers.put(userId, tradeConsumer);
            
            // Track subscriptions
            userSubscriptions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                    .addAll(symbols);

            // Get or create streaming instance (reuse if exists from quote subscription)
            net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataWebsocketInterface streaming = 
                    streamInstances.computeIfAbsent(userId, k -> {
                        net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataWebsocketInterface stream = 
                                client.stockMarketDataStream();
                        
                        // Set up listener that handles both quotes and trades
                        stream.setListener(new StockMarketDataListenerAdapter() {
                            @Override
                            public void onQuote(StockQuoteMessage quoteMessage) {
                                Consumer<StockQuoteDto> consumer = quoteConsumers.get(userId);
                                if (consumer != null && quoteMessage != null) {
                                    // Convert message directly to DTO
                                    StockQuoteDto dto = StockQuoteDto.builder()
                                            .symbol(quoteMessage.getSymbol())
                                            .askPrice(quoteMessage.getAskPrice() != null ? new java.math.BigDecimal(quoteMessage.getAskPrice()) : null)
                                            .askSize(quoteMessage.getAskSize() != null ? new java.math.BigDecimal(quoteMessage.getAskSize()) : null)
                                            .bidPrice(quoteMessage.getBidPrice() != null ? new java.math.BigDecimal(quoteMessage.getBidPrice()) : null)
                                            .bidSize(quoteMessage.getBidSize() != null ? new java.math.BigDecimal(quoteMessage.getBidSize()) : null)
                                            .timestamp(quoteMessage.getTimestamp() != null ? quoteMessage.getTimestamp().toInstant() : null)
                                            .build();
                                    consumer.accept(dto);
                                }
                            }
                            
                            @Override
                            public void onTrade(StockTradeMessage tradeMessage) {
                                Consumer<StockTradeDto> consumer = tradeConsumers.get(userId);
                                if (consumer != null && tradeMessage != null) {
                                    // Convert message directly to DTO
                                    StockTradeDto dto = StockTradeDto.builder()
                                            .symbol(tradeMessage.getSymbol())
                                            .price(tradeMessage.getPrice() != null ? new java.math.BigDecimal(tradeMessage.getPrice()) : null)
                                            .size(tradeMessage.getSize() != null ? new java.math.BigDecimal(tradeMessage.getSize()) : null)
                                            .exchange(tradeMessage.getExchange())
                                            .timestamp(tradeMessage.getTimestamp() != null ? tradeMessage.getTimestamp().toInstant() : null)
                                            .build();
                                    consumer.accept(dto);
                                }
                            }
                        });
                        
                        // Connect if not already connected
                        if (!stream.isConnected()) {
                            stream.connect();
                        }
                        
                        return stream;
                    });
            
            // Subscribe to trades (add to existing subscriptions)
            Set<String> currentTrades = streaming.getTradeSubscriptions();
            if (currentTrades == null) {
                currentTrades = new HashSet<>();
            }
            Set<String> newTrades = new HashSet<>(currentTrades);
            newTrades.addAll(symbols);
            streaming.setTradeSubscriptions(newTrades);

            log.info("Successfully subscribed to trades for {} symbols for user: {}",
                    symbols.size(), userId);
        } catch (Exception e) {
            log.error("Failed to subscribe to trades for user: {}", userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.STREAMING_ERROR,
                    "Failed to subscribe to trades: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Unsubscribes from symbols.
     *
     * @param userId  User ID
     * @param symbols Symbols to unsubscribe from
     */
    public void unsubscribe(String userId, Set<String> symbols) {
        log.info("Unsubscribing from symbols {} for user: {}", symbols, userId);

        try {
            net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataWebsocketInterface streaming = 
                    streamInstances.get(userId);
            
            if (streaming != null) {
                // Get current subscriptions
                Set<String> currentQuotes = streaming.getQuoteSubscriptions();
                Set<String> currentTrades = streaming.getTradeSubscriptions();
                
                // Remove symbols from subscriptions
                if (currentQuotes != null) {
                    Set<String> newQuotes = new HashSet<>(currentQuotes);
                    newQuotes.removeAll(symbols);
                    streaming.setQuoteSubscriptions(newQuotes);
                }
                
                if (currentTrades != null) {
                    Set<String> newTrades = new HashSet<>(currentTrades);
                    newTrades.removeAll(symbols);
                    streaming.setTradeSubscriptions(newTrades);
                }
            }

            // Update tracking
            Set<String> subs = userSubscriptions.get(userId);
            if (subs != null) {
                subs.removeAll(symbols);
            }

            log.info("Successfully unsubscribed from {} symbols for user: {}", symbols.size(), userId);
        } catch (Exception e) {
            log.error("Failed to unsubscribe from symbols for user: {}", userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.STREAMING_ERROR,
                    "Failed to unsubscribe: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Disconnects from streaming for a user.
     *
     * @param userId User ID
     */
    public void disconnect(String userId) {
        log.info("Disconnecting streaming for user: {}", userId);

        try {
            net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataWebsocketInterface streaming = 
                    streamInstances.remove(userId);
            
            if (streaming != null && streaming.isConnected()) {
                streaming.disconnect();
            }

            // Clean up
            userSubscriptions.remove(userId);
            quoteConsumers.remove(userId);
            tradeConsumers.remove(userId);

            log.info("Successfully disconnected streaming for user: {}", userId);
        } catch (Exception e) {
            log.warn("Error disconnecting streaming for user: {}", userId, e);
        }
    }

    /**
     * Gets current subscriptions for a user.
     */
    public Set<String> getSubscriptions(String userId) {
        return userSubscriptions.getOrDefault(userId, Collections.emptySet());
    }

    /**
     * Checks if a user is subscribed to any symbols.
     */
    public boolean hasActiveSubscriptions(String userId) {
        Set<String> subs = userSubscriptions.get(userId);
        return subs != null && !subs.isEmpty();
    }

}
