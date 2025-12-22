
package net.jacobpeterson.alpaca.model.util.apitype;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.SerializedName;

public enum BrokerAPIEndpointType {

    @SerializedName("sandbox")
    SANDBOX("sandbox"),
    @SerializedName("production")
    PRODUCTION("production");
    private final String value;
    private final static Map<String, BrokerAPIEndpointType> CONSTANTS = new HashMap<String, BrokerAPIEndpointType>();

    static {
        for (BrokerAPIEndpointType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    BrokerAPIEndpointType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static BrokerAPIEndpointType fromValue(String value) {
        BrokerAPIEndpointType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
