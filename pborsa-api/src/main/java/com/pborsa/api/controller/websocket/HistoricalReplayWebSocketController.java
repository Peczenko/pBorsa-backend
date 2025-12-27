package com.pborsa.api.controller.websocket;

import com.pborsa.api.domain.dto.market.HistoricalReplayControlResponse;
import com.pborsa.api.domain.dto.market.HistoricalReplayRequest;
import com.pborsa.api.domain.dto.market.HistoricalReplayTickDto;
import com.pborsa.api.service.market.HistoricalMarketDataReplayService;
import com.pborsa.api.temporal.WorkflowHistoricalReplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket controller for historical market data replay.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HistoricalReplayWebSocketController {

    private final HistoricalMarketDataReplayService replayService;
    private final WorkflowHistoricalReplayService workflowReplayService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Starts a historical replay session.
     */
    @MessageMapping("/replay/start/{userId}")
    public void startReplay(
            @Header("simpSessionId") String sessionId,
            @DestinationVariable Long userId,
            @Payload HistoricalReplayRequest request
    ) {
        String workflowId = workflowReplayService.startReplay(
                userId,
                request.symbol(),
                request.start(),
                request.end(),
                request.stepSeconds()
        );

        String replayId;
        try {
            replayId = replayService.startReplay(
                    sessionId,
                    userId,
                    request,
                    workflowId,
                    workflowReplayService::stopReplay,
                    (id, tick) -> sendReplayTick(userId, id, tick)
            );
        } catch (Exception e) {
            workflowReplayService.stopReplay(workflowId);
            throw e;
        }

        HistoricalReplayControlResponse response = HistoricalReplayControlResponse.builder()
                .replayId(replayId)
                .workflowId(workflowId)
                .status("STARTED")
                .symbol(request.symbol())
                .start(request.start())
                .end(request.end())
                .build();

        messagingTemplate.convertAndSend("/topic/replay/status/" + userId, response);
    }

    /**
     * Stops a historical replay session.
     */
    @MessageMapping("/replay/stop/{userId}/{replayId}")
    public void stopReplay(
            @DestinationVariable Long userId,
            @DestinationVariable String replayId
    ) {
        if (replayService.stopReplay(replayId).isEmpty()) {
            log.warn("No workflow associated with replay ID {}", replayId);
        }

        HistoricalReplayControlResponse response = HistoricalReplayControlResponse.builder()
                .replayId(replayId)
                .status("STOPPED")
                .build();

        messagingTemplate.convertAndSend("/topic/replay/status/" + userId, response);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        replayService.stopAllForSession(sessionId).forEach(stop -> {
            HistoricalReplayControlResponse response = HistoricalReplayControlResponse.builder()
                    .replayId(stop.replayId())
                    .workflowId(stop.workflowId())
                    .status("STOPPED")
                    .build();
            messagingTemplate.convertAndSend("/topic/replay/status/" + stop.userId(), response);
        });
    }

    private void sendReplayTick(Long userId, String replayId, HistoricalReplayTickDto tick) {
        messagingTemplate.convertAndSend(
                "/topic/replay/" + userId + "/" + replayId,
                tick
        );
    }
}
