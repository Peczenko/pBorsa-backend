
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.SerializedName;

public enum StockMarketDataMessageType {

    @SerializedName("success")
    SUCCESS("success"),
    @SerializedName("error")
    ERROR("error"),
    @SerializedName("subscription")
    SUBSCRIPTION("subscription"),
    @SerializedName("t")
    TRADES("t"),
    @SerializedName("q")
    QUOTES("q"),
    @SerializedName("b")
    MINUTE_BARS("b"),
    @SerializedName("d")
    DAILY_BARS("d"),
    @SerializedName("u")
    UPDATED_BARS("u"),
    @SerializedName("c")
    TRADE_CORRECTIONS("c"),
    @SerializedName("x")
    TRADE_CANCEL_ERRORS("x"),
    @SerializedName("l")
    LIMIT_UP_LIMIT_DOWN_BANDS("l"),
    @SerializedName("s")
    TRADING_STATUSES("s");
    private final String value;
    private final static Map<String, StockMarketDataMessageType> CONSTANTS = new HashMap<String, StockMarketDataMessageType>();

    static {
        for (StockMarketDataMessageType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    StockMarketDataMessageType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static StockMarketDataMessageType fromValue(String value) {
        StockMarketDataMessageType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
