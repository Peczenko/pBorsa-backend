package com.pborsa.api.controller.websocket;

import com.pborsa.api.domain.dto.market.HistoricalReplayControlResponse;
import com.pborsa.api.domain.dto.market.HistoricalReplayRequest;
import com.pborsa.api.domain.dto.market.HistoricalReplayTickDto;
import com.pborsa.api.service.market.HistoricalMarketDataReplayService;
import com.pborsa.api.temporal.WorkflowHistoricalReplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
            @DestinationVariable String userId,
            @Payload HistoricalReplayRequest request
    ) {
        String replayId = replayService.startReplay(
                userId,
                request,
                (id, tick) -> sendReplayTick(userId, id, tick)
        );
        String workflowId = workflowReplayService.startReplay(
                userId,
                request.symbol(),
                request.start(),
                request.end(),
                request.stepSeconds(),
                request.tickMillis()
        );
        replayService.attachWorkflow(replayId, workflowId);

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
            @DestinationVariable String userId,
            @DestinationVariable String replayId
    ) {
        replayService.stopReplay(replayId).ifPresent(workflowReplayService::stopReplay);

        HistoricalReplayControlResponse response = HistoricalReplayControlResponse.builder()
                .replayId(replayId)
                .status("STOPPED")
                .build();

        messagingTemplate.convertAndSend("/topic/replay/status/" + userId, response);
    }

    private void sendReplayTick(String userId, String replayId, HistoricalReplayTickDto tick) {
        messagingTemplate.convertAndSend(
                "/topic/replay/" + userId + "/" + replayId,
                tick
        );
    }
}
