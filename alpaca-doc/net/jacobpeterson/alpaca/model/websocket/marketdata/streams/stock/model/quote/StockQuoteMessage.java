
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.quote;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.StockMarketDataMessage;

public class StockQuoteMessage
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
     * The ask exchange code.
     * <p>
     * 
     * Corresponds to the "ax" property.
     * 
     */
    @SerializedName("ax")
    @Expose
    private java.lang.String askExchange;
    /**
     * The ask price.
     * <p>
     * 
     * Corresponds to the "ap" property.
     * 
     */
    @SerializedName("ap")
    @Expose
    private Double askPrice;
    /**
     * The ask size.
     * <p>
     * 
     * Corresponds to the "as" property.
     * 
     */
    @SerializedName("as")
    @Expose
    private Integer askSize;
    /**
     * The bid exchange code.
     * <p>
     * 
     * Corresponds to the "bx" property.
     * 
     */
    @SerializedName("bx")
    @Expose
    private java.lang.String bidExchange;
    /**
     * The bid price.
     * <p>
     * 
     * Corresponds to the "bp" property.
     * 
     */
    @SerializedName("bp")
    @Expose
    private Double bidPrice;
    /**
     * The bid size.
     * <p>
     * 
     * Corresponds to the "bs" property.
     * 
     */
    @SerializedName("bs")
    @Expose
    private Integer bidSize;
    /**
     * The {@link java.util.Set} of quote conditions.
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
    private final static long serialVersionUID = 2246749005257786993L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public StockQuoteMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public StockQuoteMessage(StockQuoteMessage source) {
        super();
        this.symbol = source.symbol;
        this.askExchange = source.askExchange;
        this.askPrice = source.askPrice;
        this.askSize = source.askSize;
        this.bidExchange = source.bidExchange;
        this.bidPrice = source.bidPrice;
        this.bidSize = source.bidSize;
        this.conditions = source.conditions;
        this.timestamp = source.timestamp;
        this.tape = source.tape;
    }

    /**
     * 
     * @param symbol
     *     The symbol.
     * @param askPrice
     *     The ask price.
     * @param tape
     *     The tape.
     * @param bidSize
     *     The bid size.
     * @param bidExchange
     *     The bid exchange code.
     * @param conditions
     *     The {@link java.util.Set} of quote conditions.
     * @param askSize
     *     The ask size.
     * @param bidPrice
     *     The bid price.
     * @param askExchange
     *     The ask exchange code.
     * @param timestamp
     *     The timestamp with nanosecond precision.
     */
    public StockQuoteMessage(java.lang.String symbol, java.lang.String askExchange, Double askPrice, Integer askSize, java.lang.String bidExchange, Double bidPrice, Integer bidSize, Set<String> conditions, OffsetDateTime timestamp, java.lang.String tape) {
        super();
        this.symbol = symbol;
        this.askExchange = askExchange;
        this.askPrice = askPrice;
        this.askSize = askSize;
        this.bidExchange = bidExchange;
        this.bidPrice = bidPrice;
        this.bidSize = bidSize;
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

    public StockQuoteMessage withSymbol(java.lang.String symbol) {
        this.symbol = symbol;
        return this;
    }

    /**
     * The ask exchange code.
     * <p>
     * 
     * Corresponds to the "ax" property.
     * 
     */
    public java.lang.String getAskExchange() {
        return askExchange;
    }

    /**
     * The ask exchange code.
     * <p>
     * 
     * Corresponds to the "ax" property.
     * 
     */
    public void setAskExchange(java.lang.String askExchange) {
        this.askExchange = askExchange;
    }

    public StockQuoteMessage withAskExchange(java.lang.String askExchange) {
        this.askExchange = askExchange;
        return this;
    }

    /**
     * The ask price.
     * <p>
     * 
     * Corresponds to the "ap" property.
     * 
     */
    public Double getAskPrice() {
        return askPrice;
    }

    /**
     * The ask price.
     * <p>
     * 
     * Corresponds to the "ap" property.
     * 
     */
    public void setAskPrice(Double askPrice) {
        this.askPrice = askPrice;
    }

    public StockQuoteMessage withAskPrice(Double askPrice) {
        this.askPrice = askPrice;
        return this;
    }

    /**
     * The ask size.
     * <p>
     * 
     * Corresponds to the "as" property.
     * 
     */
    public Integer getAskSize() {
        return askSize;
    }

    /**
     * The ask size.
     * <p>
     * 
     * Corresponds to the "as" property.
     * 
     */
    public void setAskSize(Integer askSize) {
        this.askSize = askSize;
    }

    public StockQuoteMessage withAskSize(Integer askSize) {
        this.askSize = askSize;
        return this;
    }

    /**
     * The bid exchange code.
     * <p>
     * 
     * Corresponds to the "bx" property.
     * 
     */
    public java.lang.String getBidExchange() {
        return bidExchange;
    }

    /**
     * The bid exchange code.
     * <p>
     * 
     * Corresponds to the "bx" property.
     * 
     */
    public void setBidExchange(java.lang.String bidExchange) {
        this.bidExchange = bidExchange;
    }

    public StockQuoteMessage withBidExchange(java.lang.String bidExchange) {
        this.bidExchange = bidExchange;
        return this;
    }

    /**
     * The bid price.
     * <p>
     * 
     * Corresponds to the "bp" property.
     * 
     */
    public Double getBidPrice() {
        return bidPrice;
    }

    /**
     * The bid price.
     * <p>
     * 
     * Corresponds to the "bp" property.
     * 
     */
    public void setBidPrice(Double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public StockQuoteMessage withBidPrice(Double bidPrice) {
        this.bidPrice = bidPrice;
        return this;
    }

    /**
     * The bid size.
     * <p>
     * 
     * Corresponds to the "bs" property.
     * 
     */
    public Integer getBidSize() {
        return bidSize;
    }

    /**
     * The bid size.
     * <p>
     * 
     * Corresponds to the "bs" property.
     * 
     */
    public void setBidSize(Integer bidSize) {
        this.bidSize = bidSize;
    }

    public StockQuoteMessage withBidSize(Integer bidSize) {
        this.bidSize = bidSize;
        return this;
    }

    /**
     * The {@link java.util.Set} of quote conditions.
     * <p>
     * 
     * Corresponds to the "c" property.
     * 
     */
    public Set<String> getConditions() {
        return conditions;
    }

    /**
     * The {@link java.util.Set} of quote conditions.
     * <p>
     * 
     * Corresponds to the "c" property.
     * 
     */
    public void setConditions(Set<String> conditions) {
        this.conditions = conditions;
    }

    public StockQuoteMessage withConditions(Set<String> conditions) {
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

    public StockQuoteMessage withTimestamp(OffsetDateTime timestamp) {
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

    public StockQuoteMessage withTape(java.lang.String tape) {
        this.tape = tape;
        return this;
    }

    @Override
    public java.lang.String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StockQuoteMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("askExchange");
        sb.append('=');
        sb.append(((this.askExchange == null)?"<null>":this.askExchange));
        sb.append(',');
        sb.append("askPrice");
        sb.append('=');
        sb.append(((this.askPrice == null)?"<null>":this.askPrice));
        sb.append(',');
        sb.append("askSize");
        sb.append('=');
        sb.append(((this.askSize == null)?"<null>":this.askSize));
        sb.append(',');
        sb.append("bidExchange");
        sb.append('=');
        sb.append(((this.bidExchange == null)?"<null>":this.bidExchange));
        sb.append(',');
        sb.append("bidPrice");
        sb.append('=');
        sb.append(((this.bidPrice == null)?"<null>":this.bidPrice));
        sb.append(',');
        sb.append("bidSize");
        sb.append('=');
        sb.append(((this.bidSize == null)?"<null>":this.bidSize));
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
        result = ((result* 31)+((this.askPrice == null)? 0 :this.askPrice.hashCode()));
        result = ((result* 31)+((this.tape == null)? 0 :this.tape.hashCode()));
        result = ((result* 31)+((this.bidSize == null)? 0 :this.bidSize.hashCode()));
        result = ((result* 31)+((this.bidExchange == null)? 0 :this.bidExchange.hashCode()));
        result = ((result* 31)+((this.conditions == null)? 0 :this.conditions.hashCode()));
        result = ((result* 31)+((this.askSize == null)? 0 :this.askSize.hashCode()));
        result = ((result* 31)+((this.bidPrice == null)? 0 :this.bidPrice.hashCode()));
        result = ((result* 31)+((this.askExchange == null)? 0 :this.askExchange.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StockQuoteMessage) == false) {
            return false;
        }
        StockQuoteMessage rhs = ((StockQuoteMessage) other);
        return ((((((((((super.equals(rhs)&&((this.symbol == rhs.symbol)||((this.symbol!= null)&&this.symbol.equals(rhs.symbol))))&&((this.askPrice == rhs.askPrice)||((this.askPrice!= null)&&this.askPrice.equals(rhs.askPrice))))&&((this.tape == rhs.tape)||((this.tape!= null)&&this.tape.equals(rhs.tape))))&&((this.bidSize == rhs.bidSize)||((this.bidSize!= null)&&this.bidSize.equals(rhs.bidSize))))&&((this.bidExchange == rhs.bidExchange)||((this.bidExchange!= null)&&this.bidExchange.equals(rhs.bidExchange))))&&((this.conditions == rhs.conditions)||((this.conditions!= null)&&this.conditions.equals(rhs.conditions))))&&((this.askSize == rhs.askSize)||((this.askSize!= null)&&this.askSize.equals(rhs.askSize))))&&((this.bidPrice == rhs.bidPrice)||((this.bidPrice!= null)&&this.bidPrice.equals(rhs.bidPrice))))&&((this.askExchange == rhs.askExchange)||((this.askExchange!= null)&&this.askExchange.equals(rhs.askExchange))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}
