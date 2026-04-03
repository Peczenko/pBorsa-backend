package com.pborsa.api.market.controller.websocket;

import com.pborsa.domain.dto.market.StockQuoteDto;
import com.pborsa.domain.dto.market.StockTradeDto;
import com.pborsa.trading.market.RealTimeMarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Set;

/**
 * WebSocket controller for real-time market data streaming.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MarketDataWebSocketController {

    private final RealTimeMarketDataService realTimeMarketDataService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Subscribes to real-time quotes.
     */
    @MessageMapping("/subscribe/quotes/{userId}")
    public void subscribeToQuotes(
            @DestinationVariable Long userId,
            @Payload Set<String> symbols
    ) {
        log.info("WebSocket: User {} subscribing to quotes for: {}", userId, symbols);
        
        realTimeMarketDataService.subscribeToQuotes(userId, symbols, quote -> sendQuoteToUser(userId, quote));
    }

    /**
     * Subscribes to real-time trades.
     */
    @MessageMapping("/subscribe/trades/{userId}")
    public void subscribeToTrades(
            @DestinationVariable Long userId,
            @Payload Set<String> symbols
    ) {
        log.info("WebSocket: User {} subscribing to trades for: {}", userId, symbols);
        
        realTimeMarketDataService.subscribeToTrades(userId, symbols, trade -> {
            log.info("Pushing trade to {}: {}", userId, trade);
            sendTradeToUser(userId, trade);
        });
    }

    /**
     * Unsubscribes from symbols.
     */
    @MessageMapping("/unsubscribe/{userId}")
    public void unsubscribe(
            @DestinationVariable Long userId,
            @Payload Set<String> symbols
    ) {
        log.info("WebSocket: User {} unsubscribing from: {}", userId, symbols);
        realTimeMarketDataService.unsubscribe(userId, symbols);
    }

    /**
     * Disconnects user's WebSocket connection.
     */
    @MessageMapping("/disconnect/{userId}")
    public void disconnect(@DestinationVariable Long userId) {
        log.info("WebSocket: User {} disconnecting", userId);
        realTimeMarketDataService.disconnect(userId);
    }

    /**
     * Sends a quote to a specific user.
     */
    private void sendQuoteToUser(Long userId, StockQuoteDto quote) {
        messagingTemplate.convertAndSend(
                "/topic/quotes/" + userId,
                quote
        );
    }

    /**
     * Sends a trade to a specific user.
     */
    private void sendTradeToUser(Long userId, StockTradeDto trade) {
        messagingTemplate.convertAndSend(
                "/topic/trades/" + userId,
                trade
        );
    }
}
