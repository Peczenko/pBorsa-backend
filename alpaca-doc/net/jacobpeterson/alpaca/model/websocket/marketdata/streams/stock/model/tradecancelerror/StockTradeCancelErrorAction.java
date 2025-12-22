
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.tradecancelerror;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.SerializedName;

public enum StockTradeCancelErrorAction {

    @SerializedName("C")
    CANCEL("C"),
    @SerializedName("E")
    ERROR("E");
    private final String value;
    private final static Map<String, StockTradeCancelErrorAction> CONSTANTS = new HashMap<String, StockTradeCancelErrorAction>();

    static {
        for (StockTradeCancelErrorAction c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    StockTradeCancelErrorAction(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static StockTradeCancelErrorAction fromValue(String value) {
        StockTradeCancelErrorAction constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
