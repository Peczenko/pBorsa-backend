package com.pborsa.api.service.trading;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OrderStatusMessageNormalizer {

    private static final String RESPONSE_BODY_MARKER = "HTTP response body:";
    private static final String RESPONSE_HEADERS_MARKER = "HTTP response headers:";
    private static final int MESSAGE_MAX_LENGTH = 512;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private OrderStatusMessageNormalizer() {
    }

    public static String normalize(String message) {
        if (message == null || message.isBlank()) {
            return message;
        }
        String extracted = extractHttpErrorMessage(message);
        String result = extracted != null ? extracted : message.trim();
        if (result.length() > MESSAGE_MAX_LENGTH) {
            return result.substring(0, MESSAGE_MAX_LENGTH);
        }
        return result;
    }

    private static String extractHttpErrorMessage(String message) {
        int bodyIndex = message.indexOf(RESPONSE_BODY_MARKER);
        if (bodyIndex < 0) {
            return null;
        }
        int start = bodyIndex + RESPONSE_BODY_MARKER.length();
        int headersIndex = message.indexOf(RESPONSE_HEADERS_MARKER, start);
        String body = headersIndex >= 0
                ? message.substring(start, headersIndex)
                : message.substring(start);
        body = body.trim();
        if (body.isEmpty()) {
            return null;
        }
        if (body.startsWith("{") || body.startsWith("[")) {
            try {
                JsonNode node = OBJECT_MAPPER.readTree(body);
                JsonNode messageNode = node.get("message");
                if (messageNode != null && !messageNode.isNull()) {
                    return messageNode.asText();
                }
            } catch (Exception e) {
                log.debug("Failed to parse error message JSON body: {}", body, e);
            }
        }
        return body;
    }
}
