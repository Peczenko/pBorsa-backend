
package net.jacobpeterson.alpaca.model.websocket.updates.model;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.SerializedName;

public enum UpdatesMessageType {

    @SerializedName("listening")
    LISTENING("listening"),
    @SerializedName("authorization")
    AUTHORIZATION("authorization"),
    @SerializedName("trade_updates")
    TRADE_UPDATES("trade_updates");
    private final String value;
    private final static Map<String, UpdatesMessageType> CONSTANTS = new HashMap<String, UpdatesMessageType>();

    static {
        for (UpdatesMessageType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    UpdatesMessageType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static UpdatesMessageType fromValue(String value) {
        UpdatesMessageType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
