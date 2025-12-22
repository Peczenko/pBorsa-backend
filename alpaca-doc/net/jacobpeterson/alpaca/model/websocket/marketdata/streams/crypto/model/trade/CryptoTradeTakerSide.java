
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.trade;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.SerializedName;

public enum CryptoTradeTakerSide {

    @SerializedName("B")
    BUY("B"),
    @SerializedName("S")
    SELL("S");
    private final String value;
    private final static Map<String, CryptoTradeTakerSide> CONSTANTS = new HashMap<String, CryptoTradeTakerSide>();

    static {
        for (CryptoTradeTakerSide c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    CryptoTradeTakerSide(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static CryptoTradeTakerSide fromValue(String value) {
        CryptoTradeTakerSide constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
