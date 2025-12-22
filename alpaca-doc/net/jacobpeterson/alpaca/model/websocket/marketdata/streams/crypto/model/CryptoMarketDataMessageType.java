
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.SerializedName;

public enum CryptoMarketDataMessageType {

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
    @SerializedName("o")
    ORDER_BOOKS("o");
    private final String value;
    private final static Map<String, CryptoMarketDataMessageType> CONSTANTS = new HashMap<String, CryptoMarketDataMessageType>();

    static {
        for (CryptoMarketDataMessageType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    CryptoMarketDataMessageType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static CryptoMarketDataMessageType fromValue(String value) {
        CryptoMarketDataMessageType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
