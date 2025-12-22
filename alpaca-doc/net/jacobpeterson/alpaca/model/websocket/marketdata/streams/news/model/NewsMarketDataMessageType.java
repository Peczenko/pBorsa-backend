
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.news.model;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.SerializedName;

public enum NewsMarketDataMessageType {

    @SerializedName("success")
    SUCCESS("success"),
    @SerializedName("error")
    ERROR("error"),
    @SerializedName("subscription")
    SUBSCRIPTION("subscription"),
    @SerializedName("n")
    NEWS("n");
    private final String value;
    private final static Map<String, NewsMarketDataMessageType> CONSTANTS = new HashMap<String, NewsMarketDataMessageType>();

    static {
        for (NewsMarketDataMessageType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    NewsMarketDataMessageType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static NewsMarketDataMessageType fromValue(String value) {
        NewsMarketDataMessageType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
