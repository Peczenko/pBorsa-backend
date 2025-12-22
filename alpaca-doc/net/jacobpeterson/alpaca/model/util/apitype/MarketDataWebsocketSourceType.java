
package net.jacobpeterson.alpaca.model.util.apitype;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.SerializedName;

public enum MarketDataWebsocketSourceType {

    @SerializedName("iex")
    IEX("iex"),
    @SerializedName("sip")
    SIP("sip");
    private final String value;
    private final static Map<String, MarketDataWebsocketSourceType> CONSTANTS = new HashMap<String, MarketDataWebsocketSourceType>();

    static {
        for (MarketDataWebsocketSourceType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    MarketDataWebsocketSourceType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static MarketDataWebsocketSourceType fromValue(String value) {
        MarketDataWebsocketSourceType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
