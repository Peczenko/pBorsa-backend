
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.CryptoMarketDataMessage;

public class CryptoOrderBookMessage
    extends CryptoMarketDataMessage
    implements Serializable
{

    /**
     * The symbol.
     * <p>
     * 
     * Corresponds to the "S" property.
     * 
     */
    @SerializedName("S")
    @Expose
    private String symbol;
    /**
     * The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry} asks.
     * <p>
     * 
     * Corresponds to the "a" property.
     * 
     */
    @SerializedName("a")
    @Expose
    private Set<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry> asks = new LinkedHashSet<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry>();
    /**
     * The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry} bids.
     * <p>
     * 
     * Corresponds to the "b" property.
     * 
     */
    @SerializedName("b")
    @Expose
    private Set<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry> bids = new LinkedHashSet<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry>();
    /**
     * The timestamp with nanosecond precision.
     * <p>
     * 
     * Corresponds to the "t" property.
     * 
     */
    @SerializedName("t")
    @Expose
    private OffsetDateTime timestamp;
    /**
     * Whether this particular {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookMessage} contains the whole order book and not just an update. Typically <code>true</code> for the first message and <code>false</code> for subsequent messages.
     * <p>
     * 
     * Corresponds to the "r" property.
     * 
     */
    @SerializedName("r")
    @Expose
    private Boolean reset;
    private final static long serialVersionUID = -2889357455669346519L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CryptoOrderBookMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public CryptoOrderBookMessage(CryptoOrderBookMessage source) {
        super();
        this.symbol = source.symbol;
        this.asks = source.asks;
        this.bids = source.bids;
        this.timestamp = source.timestamp;
        this.reset = source.reset;
    }

    /**
     * 
     * @param symbol
     *     The symbol.
     * @param asks
     *     The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry} asks.
     * @param bids
     *     The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry} bids.
     * @param reset
     *     Whether this particular {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookMessage} contains the whole order book and not just an update. Typically <code>true</code> for the first message and <code>false</code> for subsequent messages.
     * @param timestamp
     *     The timestamp with nanosecond precision.
     */
    public CryptoOrderBookMessage(String symbol, Set<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry> asks, Set<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry> bids, OffsetDateTime timestamp, Boolean reset) {
        super();
        this.symbol = symbol;
        this.asks = asks;
        this.bids = bids;
        this.timestamp = timestamp;
        this.reset = reset;
    }

    /**
     * The symbol.
     * <p>
     * 
     * Corresponds to the "S" property.
     * 
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * The symbol.
     * <p>
     * 
     * Corresponds to the "S" property.
     * 
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public CryptoOrderBookMessage withSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    /**
     * The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry} asks.
     * <p>
     * 
     * Corresponds to the "a" property.
     * 
     */
    public Set<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry> getAsks() {
        return asks;
    }

    /**
     * The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry} asks.
     * <p>
     * 
     * Corresponds to the "a" property.
     * 
     */
    public void setAsks(Set<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry> asks) {
        this.asks = asks;
    }

    public CryptoOrderBookMessage withAsks(Set<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry> asks) {
        this.asks = asks;
        return this;
    }

    /**
     * The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry} bids.
     * <p>
     * 
     * Corresponds to the "b" property.
     * 
     */
    public Set<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry> getBids() {
        return bids;
    }

    /**
     * The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry} bids.
     * <p>
     * 
     * Corresponds to the "b" property.
     * 
     */
    public void setBids(Set<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry> bids) {
        this.bids = bids;
    }

    public CryptoOrderBookMessage withBids(Set<net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookEntry> bids) {
        this.bids = bids;
        return this;
    }

    /**
     * The timestamp with nanosecond precision.
     * <p>
     * 
     * Corresponds to the "t" property.
     * 
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * The timestamp with nanosecond precision.
     * <p>
     * 
     * Corresponds to the "t" property.
     * 
     */
    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public CryptoOrderBookMessage withTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Whether this particular {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookMessage} contains the whole order book and not just an update. Typically <code>true</code> for the first message and <code>false</code> for subsequent messages.
     * <p>
     * 
     * Corresponds to the "r" property.
     * 
     */
    public Boolean getReset() {
        return reset;
    }

    /**
     * Whether this particular {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook.CryptoOrderBookMessage} contains the whole order book and not just an update. Typically <code>true</code> for the first message and <code>false</code> for subsequent messages.
     * <p>
     * 
     * Corresponds to the "r" property.
     * 
     */
    public void setReset(Boolean reset) {
        this.reset = reset;
    }

    public CryptoOrderBookMessage withReset(Boolean reset) {
        this.reset = reset;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CryptoOrderBookMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        int baseLength = sb.length();
        String superString = super.toString();
        if (superString!= null) {
            int contentStart = superString.indexOf('[');
            int contentEnd = superString.lastIndexOf(']');
            if ((contentStart >= 0)&&(contentEnd >contentStart)) {
                sb.append(superString, (contentStart + 1), contentEnd);
            } else {
                sb.append(superString);
            }
        }
        if (sb.length()>baseLength) {
            sb.append(',');
        }
        sb.append("symbol");
        sb.append('=');
        sb.append(((this.symbol == null)?"<null>":this.symbol));
        sb.append(',');
        sb.append("asks");
        sb.append('=');
        sb.append(((this.asks == null)?"<null>":this.asks));
        sb.append(',');
        sb.append("bids");
        sb.append('=');
        sb.append(((this.bids == null)?"<null>":this.bids));
        sb.append(',');
        sb.append("timestamp");
        sb.append('=');
        sb.append(((this.timestamp == null)?"<null>":this.timestamp));
        sb.append(',');
        sb.append("reset");
        sb.append('=');
        sb.append(((this.reset == null)?"<null>":this.reset));
        sb.append(',');
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
        result = ((result* 31)+((this.bids == null)? 0 :this.bids.hashCode()));
        result = ((result* 31)+((this.symbol == null)? 0 :this.symbol.hashCode()));
        result = ((result* 31)+((this.reset == null)? 0 :this.reset.hashCode()));
        result = ((result* 31)+((this.asks == null)? 0 :this.asks.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CryptoOrderBookMessage) == false) {
            return false;
        }
        CryptoOrderBookMessage rhs = ((CryptoOrderBookMessage) other);
        return (((((super.equals(rhs)&&((this.bids == rhs.bids)||((this.bids!= null)&&this.bids.equals(rhs.bids))))&&((this.symbol == rhs.symbol)||((this.symbol!= null)&&this.symbol.equals(rhs.symbol))))&&((this.reset == rhs.reset)||((this.reset!= null)&&this.reset.equals(rhs.reset))))&&((this.asks == rhs.asks)||((this.asks!= null)&&this.asks.equals(rhs.asks))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}
