
package net.jacobpeterson.alpaca.model.websocket.marketdata.model;

import java.io.Serializable;

public class MarketDataMessage implements Serializable
{

    private final static long serialVersionUID = -5455215837804476338L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(MarketDataMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MarketDataMessage) == false) {
            return false;
        }
        MarketDataMessage rhs = ((MarketDataMessage) other);
        return true;
    }

}
