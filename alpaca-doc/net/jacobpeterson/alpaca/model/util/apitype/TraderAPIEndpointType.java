
package net.jacobpeterson.alpaca.model.util.apitype;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.SerializedName;

public enum TraderAPIEndpointType {

    @SerializedName("paper")
    PAPER("paper"),
    @SerializedName("live")
    LIVE("live");
    private final String value;
    private final static Map<String, TraderAPIEndpointType> CONSTANTS = new HashMap<String, TraderAPIEndpointType>();

    static {
        for (TraderAPIEndpointType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    TraderAPIEndpointType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static TraderAPIEndpointType fromValue(String value) {
        TraderAPIEndpointType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
