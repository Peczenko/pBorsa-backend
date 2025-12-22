
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.trade;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.StockMarketDataMessage;

public class StockTradeMessage
    extends StockMarketDataMessage
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
    private java.lang.String symbol;
    /**
     * The trade ID.
     * <p>
     * 
     * Corresponds to the "i" property.
     * 
     */
    @SerializedName("i")
    @Expose
    private Long tradeID;
    /**
     * The exchange code where the trade occurred.
     * <p>
     * 
     * Corresponds to the "x" property.
     * 
     */
    @SerializedName("x")
    @Expose
    private java.lang.String exchange;
    /**
     * The trade price.
     * <p>
     * 
     * Corresponds to the "p" property.
     * 
     */
    @SerializedName("p")
    @Expose
    private Double price;
    /**
     * The trade size.
     * <p>
     * 
     * Corresponds to the "s" property.
     * 
     */
    @SerializedName("s")
    @Expose
    private Integer size;
    /**
     * The {@link java.util.Set} of trade conditions.
     * <p>
     * 
     * Corresponds to the "c" property.
     * 
     */
    @SerializedName("c")
    @Expose
    private Set<String> conditions = new LinkedHashSet<String>();
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
     * The tape.
     * <p>
     * 
     * Corresponds to the "z" property.
     * 
     */
    @SerializedName("z")
    @Expose
    private java.lang.String tape;
    private final static long serialVersionUID = 1010515516701535430L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public StockTradeMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public StockTradeMessage(StockTradeMessage source) {
        super();
        this.symbol = source.symbol;
        this.tradeID = source.tradeID;
        this.exchange = source.exchange;
        this.price = source.price;
        this.size = source.size;
        this.conditions = source.conditions;
        this.timestamp = source.timestamp;
        this.tape = source.tape;
    }

    /**
     * 
     * @param symbol
     *     The symbol.
     * @param size
     *     The trade size.
     * @param tape
     *     The tape.
     * @param price
     *     The trade price.
     * @param exchange
     *     The exchange code where the trade occurred.
     * @param conditions
     *     The {@link java.util.Set} of trade conditions.
     * @param tradeID
     *     The trade ID.
     * @param timestamp
     *     The timestamp with nanosecond precision.
     */
    public StockTradeMessage(java.lang.String symbol, Long tradeID, java.lang.String exchange, Double price, Integer size, Set<String> conditions, OffsetDateTime timestamp, java.lang.String tape) {
        super();
        this.symbol = symbol;
        this.tradeID = tradeID;
        this.exchange = exchange;
        this.price = price;
        this.size = size;
        this.conditions = conditions;
        this.timestamp = timestamp;
        this.tape = tape;
    }

    /**
     * The symbol.
     * <p>
     * 
     * Corresponds to the "S" property.
     * 
     */
    public java.lang.String getSymbol() {
        return symbol;
    }

    /**
     * The symbol.
     * <p>
     * 
     * Corresponds to the "S" property.
     * 
     */
    public void setSymbol(java.lang.String symbol) {
        this.symbol = symbol;
    }

    public StockTradeMessage withSymbol(java.lang.String symbol) {
        this.symbol = symbol;
        return this;
    }

    /**
     * The trade ID.
     * <p>
     * 
     * Corresponds to the "i" property.
     * 
     */
    public Long getTradeID() {
        return tradeID;
    }

    /**
     * The trade ID.
     * <p>
     * 
     * Corresponds to the "i" property.
     * 
     */
    public void setTradeID(Long tradeID) {
        this.tradeID = tradeID;
    }

    public StockTradeMessage withTradeID(Long tradeID) {
        this.tradeID = tradeID;
        return this;
    }

    /**
     * The exchange code where the trade occurred.
     * <p>
     * 
     * Corresponds to the "x" property.
     * 
     */
    public java.lang.String getExchange() {
        return exchange;
    }

    /**
     * The exchange code where the trade occurred.
     * <p>
     * 
     * Corresponds to the "x" property.
     * 
     */
    public void setExchange(java.lang.String exchange) {
        this.exchange = exchange;
    }

    public StockTradeMessage withExchange(java.lang.String exchange) {
        this.exchange = exchange;
        return this;
    }

    /**
     * The trade price.
     * <p>
     * 
     * Corresponds to the "p" property.
     * 
     */
    public Double getPrice() {
        return price;
    }

    /**
     * The trade price.
     * <p>
     * 
     * Corresponds to the "p" property.
     * 
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    public StockTradeMessage withPrice(Double price) {
        this.price = price;
        return this;
    }

    /**
     * The trade size.
     * <p>
     * 
     * Corresponds to the "s" property.
     * 
     */
    public Integer getSize() {
        return size;
    }

    /**
     * The trade size.
     * <p>
     * 
     * Corresponds to the "s" property.
     * 
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    public StockTradeMessage withSize(Integer size) {
        this.size = size;
        return this;
    }

    /**
     * The {@link java.util.Set} of trade conditions.
     * <p>
     * 
     * Corresponds to the "c" property.
     * 
     */
    public Set<String> getConditions() {
        return conditions;
    }

    /**
     * The {@link java.util.Set} of trade conditions.
     * <p>
     * 
     * Corresponds to the "c" property.
     * 
     */
    public void setConditions(Set<String> conditions) {
        this.conditions = conditions;
    }

    public StockTradeMessage withConditions(Set<String> conditions) {
        this.conditions = conditions;
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

    public StockTradeMessage withTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * The tape.
     * <p>
     * 
     * Corresponds to the "z" property.
     * 
     */
    public java.lang.String getTape() {
        return tape;
    }

    /**
     * The tape.
     * <p>
     * 
     * Corresponds to the "z" property.
     * 
     */
    public void setTape(java.lang.String tape) {
        this.tape = tape;
    }

    public StockTradeMessage withTape(java.lang.String tape) {
        this.tape = tape;
        return this;
    }

    @Override
    public java.lang.String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StockTradeMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        int baseLength = sb.length();
        java.lang.String superString = super.toString();
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
        sb.append("tradeID");
        sb.append('=');
        sb.append(((this.tradeID == null)?"<null>":this.tradeID));
        sb.append(',');
        sb.append("exchange");
        sb.append('=');
        sb.append(((this.exchange == null)?"<null>":this.exchange));
        sb.append(',');
        sb.append("price");
        sb.append('=');
        sb.append(((this.price == null)?"<null>":this.price));
        sb.append(',');
        sb.append("size");
        sb.append('=');
        sb.append(((this.size == null)?"<null>":this.size));
        sb.append(',');
        sb.append("conditions");
        sb.append('=');
        sb.append(((this.conditions == null)?"<null>":this.conditions));
        sb.append(',');
        sb.append("timestamp");
        sb.append('=');
        sb.append(((this.timestamp == null)?"<null>":this.timestamp));
        sb.append(',');
        sb.append("tape");
        sb.append('=');
        sb.append(((this.tape == null)?"<null>":this.tape));
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
        result = ((result* 31)+((this.symbol == null)? 0 :this.symbol.hashCode()));
        result = ((result* 31)+((this.size == null)? 0 :this.size.hashCode()));
        result = ((result* 31)+((this.tape == null)? 0 :this.tape.hashCode()));
        result = ((result* 31)+((this.price == null)? 0 :this.price.hashCode()));
        result = ((result* 31)+((this.exchange == null)? 0 :this.exchange.hashCode()));
        result = ((result* 31)+((this.conditions == null)? 0 :this.conditions.hashCode()));
        result = ((result* 31)+((this.tradeID == null)? 0 :this.tradeID.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StockTradeMessage) == false) {
            return false;
        }
        StockTradeMessage rhs = ((StockTradeMessage) other);
        return ((((((((super.equals(rhs)&&((this.symbol == rhs.symbol)||((this.symbol!= null)&&this.symbol.equals(rhs.symbol))))&&((this.size == rhs.size)||((this.size!= null)&&this.size.equals(rhs.size))))&&((this.tape == rhs.tape)||((this.tape!= null)&&this.tape.equals(rhs.tape))))&&((this.price == rhs.price)||((this.price!= null)&&this.price.equals(rhs.price))))&&((this.exchange == rhs.exchange)||((this.exchange!= null)&&this.exchange.equals(rhs.exchange))))&&((this.conditions == rhs.conditions)||((this.conditions!= null)&&this.conditions.equals(rhs.conditions))))&&((this.tradeID == rhs.tradeID)||((this.tradeID!= null)&&this.tradeID.equals(rhs.tradeID))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}
