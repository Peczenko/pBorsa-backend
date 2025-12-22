
package net.jacobpeterson.alpaca.model.websocket.marketdata.model.control;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.SerializedName;

public enum SuccessMessageType {

    @SerializedName("success")
    SUCCESS("success"),
    @SerializedName("authenticated")
    AUTHENTICATED("authenticated");
    private final String value;
    private final static Map<String, SuccessMessageType> CONSTANTS = new HashMap<String, SuccessMessageType>();

    static {
        for (SuccessMessageType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    SuccessMessageType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static SuccessMessageType fromValue(String value) {
        SuccessMessageType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
