
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.tradecorrection;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.StockMarketDataMessage;

public class StockTradeCorrectionMessage
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
     * The exchange code where the trade correction occurred.
     * <p>
     * 
     * Corresponds to the "x" property.
     * 
     */
    @SerializedName("x")
    @Expose
    private java.lang.String exchange;
    /**
     * The original trade ID.
     * <p>
     * 
     * Corresponds to the "oi" property.
     * 
     */
    @SerializedName("oi")
    @Expose
    private Integer originalTradeID;
    /**
     * The original trade price.
     * <p>
     * 
     * Corresponds to the "op" property.
     * 
     */
    @SerializedName("op")
    @Expose
    private Double originalPrice;
    /**
     * The original trade size.
     * <p>
     * 
     * Corresponds to the "os" property.
     * 
     */
    @SerializedName("os")
    @Expose
    private Integer originalSize;
    /**
     * The {@link java.util.Set} of original trade conditions.
     * <p>
     * 
     * Corresponds to the "oc" property.
     * 
     */
    @SerializedName("oc")
    @Expose
    private Set<String> originalConditions = new LinkedHashSet<String>();
    /**
     * The corrected trade ID.
     * <p>
     * 
     * Corresponds to the "ci" property.
     * 
     */
    @SerializedName("ci")
    @Expose
    private Integer correctedTradeID;
    /**
     * The corrected trade price.
     * <p>
     * 
     * Corresponds to the "cp" property.
     * 
     */
    @SerializedName("cp")
    @Expose
    private Double correctedPrice;
    /**
     * The corrected trade size.
     * <p>
     * 
     * Corresponds to the "cs" property.
     * 
     */
    @SerializedName("cs")
    @Expose
    private Integer correctedSize;
    /**
     * The {@link java.util.Set} of original trade conditions.
     * <p>
     * 
     * Corresponds to the "cc" property.
     * 
     */
    @SerializedName("cc")
    @Expose
    private Set<String> correctedConditions = new LinkedHashSet<String>();
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
    private final static long serialVersionUID = -7210073670979789876L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public StockTradeCorrectionMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public StockTradeCorrectionMessage(StockTradeCorrectionMessage source) {
        super();
        this.symbol = source.symbol;
        this.exchange = source.exchange;
        this.originalTradeID = source.originalTradeID;
        this.originalPrice = source.originalPrice;
        this.originalSize = source.originalSize;
        this.originalConditions = source.originalConditions;
        this.correctedTradeID = source.correctedTradeID;
        this.correctedPrice = source.correctedPrice;
        this.correctedSize = source.correctedSize;
        this.correctedConditions = source.correctedConditions;
        this.timestamp = source.timestamp;
        this.tape = source.tape;
    }

    /**
     * 
     * @param correctedTradeID
     *     The corrected trade ID.
     * @param symbol
     *     The symbol.
     * @param originalPrice
     *     The original trade price.
     * @param correctedConditions
     *     The {@link java.util.Set} of original trade conditions.
     * @param tape
     *     The tape.
     * @param correctedPrice
     *     The corrected trade price.
     * @param exchange
     *     The exchange code where the trade correction occurred.
     * @param originalSize
     *     The original trade size.
     * @param correctedSize
     *     The corrected trade size.
     * @param originalTradeID
     *     The original trade ID.
     * @param originalConditions
     *     The {@link java.util.Set} of original trade conditions.
     * @param timestamp
     *     The timestamp with nanosecond precision.
     */
    public StockTradeCorrectionMessage(java.lang.String symbol, java.lang.String exchange, Integer originalTradeID, Double originalPrice, Integer originalSize, Set<String> originalConditions, Integer correctedTradeID, Double correctedPrice, Integer correctedSize, Set<String> correctedConditions, OffsetDateTime timestamp, java.lang.String tape) {
        super();
        this.symbol = symbol;
        this.exchange = exchange;
        this.originalTradeID = originalTradeID;
        this.originalPrice = originalPrice;
        this.originalSize = originalSize;
        this.originalConditions = originalConditions;
        this.correctedTradeID = correctedTradeID;
        this.correctedPrice = correctedPrice;
        this.correctedSize = correctedSize;
        this.correctedConditions = correctedConditions;
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

    public StockTradeCorrectionMessage withSymbol(java.lang.String symbol) {
        this.symbol = symbol;
        return this;
    }

    /**
     * The exchange code where the trade correction occurred.
     * <p>
     * 
     * Corresponds to the "x" property.
     * 
     */
    public java.lang.String getExchange() {
        return exchange;
    }

    /**
     * The exchange code where the trade correction occurred.
     * <p>
     * 
     * Corresponds to the "x" property.
     * 
     */
    public void setExchange(java.lang.String exchange) {
        this.exchange = exchange;
    }

    public StockTradeCorrectionMessage withExchange(java.lang.String exchange) {
        this.exchange = exchange;
        return this;
    }

    /**
     * The original trade ID.
     * <p>
     * 
     * Corresponds to the "oi" property.
     * 
     */
    public Integer getOriginalTradeID() {
        return originalTradeID;
    }

    /**
     * The original trade ID.
     * <p>
     * 
     * Corresponds to the "oi" property.
     * 
     */
    public void setOriginalTradeID(Integer originalTradeID) {
        this.originalTradeID = originalTradeID;
    }

    public StockTradeCorrectionMessage withOriginalTradeID(Integer originalTradeID) {
        this.originalTradeID = originalTradeID;
        return this;
    }

    /**
     * The original trade price.
     * <p>
     * 
     * Corresponds to the "op" property.
     * 
     */
    public Double getOriginalPrice() {
        return originalPrice;
    }

    /**
     * The original trade price.
     * <p>
     * 
     * Corresponds to the "op" property.
     * 
     */
    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public StockTradeCorrectionMessage withOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
        return this;
    }

    /**
     * The original trade size.
     * <p>
     * 
     * Corresponds to the "os" property.
     * 
     */
    public Integer getOriginalSize() {
        return originalSize;
    }

    /**
     * The original trade size.
     * <p>
     * 
     * Corresponds to the "os" property.
     * 
     */
    public void setOriginalSize(Integer originalSize) {
        this.originalSize = originalSize;
    }

    public StockTradeCorrectionMessage withOriginalSize(Integer originalSize) {
        this.originalSize = originalSize;
        return this;
    }

    /**
     * The {@link java.util.Set} of original trade conditions.
     * <p>
     * 
     * Corresponds to the "oc" property.
     * 
     */
    public Set<String> getOriginalConditions() {
        return originalConditions;
    }

    /**
     * The {@link java.util.Set} of original trade conditions.
     * <p>
     * 
     * Corresponds to the "oc" property.
     * 
     */
    public void setOriginalConditions(Set<String> originalConditions) {
        this.originalConditions = originalConditions;
    }

    public StockTradeCorrectionMessage withOriginalConditions(Set<String> originalConditions) {
        this.originalConditions = originalConditions;
        return this;
    }

    /**
     * The corrected trade ID.
     * <p>
     * 
     * Corresponds to the "ci" property.
     * 
     */
    public Integer getCorrectedTradeID() {
        return correctedTradeID;
    }

    /**
     * The corrected trade ID.
     * <p>
     * 
     * Corresponds to the "ci" property.
     * 
     */
    public void setCorrectedTradeID(Integer correctedTradeID) {
        this.correctedTradeID = correctedTradeID;
    }

    public StockTradeCorrectionMessage withCorrectedTradeID(Integer correctedTradeID) {
        this.correctedTradeID = correctedTradeID;
        return this;
    }

    /**
     * The corrected trade price.
     * <p>
     * 
     * Corresponds to the "cp" property.
     * 
     */
    public Double getCorrectedPrice() {
        return correctedPrice;
    }

    /**
     * The corrected trade price.
     * <p>
     * 
     * Corresponds to the "cp" property.
     * 
     */
    public void setCorrectedPrice(Double correctedPrice) {
        this.correctedPrice = correctedPrice;
    }

    public StockTradeCorrectionMessage withCorrectedPrice(Double correctedPrice) {
        this.correctedPrice = correctedPrice;
        return this;
    }

    /**
     * The corrected trade size.
     * <p>
     * 
     * Corresponds to the "cs" property.
     * 
     */
    public Integer getCorrectedSize() {
        return correctedSize;
    }

    /**
     * The corrected trade size.
     * <p>
     * 
     * Corresponds to the "cs" property.
     * 
     */
    public void setCorrectedSize(Integer correctedSize) {
        this.correctedSize = correctedSize;
    }

    public StockTradeCorrectionMessage withCorrectedSize(Integer correctedSize) {
        this.correctedSize = correctedSize;
        return this;
    }

    /**
     * The {@link java.util.Set} of original trade conditions.
     * <p>
     * 
     * Corresponds to the "cc" property.
     * 
     */
    public Set<String> getCorrectedConditions() {
        return correctedConditions;
    }

    /**
     * The {@link java.util.Set} of original trade conditions.
     * <p>
     * 
     * Corresponds to the "cc" property.
     * 
     */
    public void setCorrectedConditions(Set<String> correctedConditions) {
        this.correctedConditions = correctedConditions;
    }

    public StockTradeCorrectionMessage withCorrectedConditions(Set<String> correctedConditions) {
        this.correctedConditions = correctedConditions;
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

    public StockTradeCorrectionMessage withTimestamp(OffsetDateTime timestamp) {
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

    public StockTradeCorrectionMessage withTape(java.lang.String tape) {
        this.tape = tape;
        return this;
    }

    @Override
    public java.lang.String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StockTradeCorrectionMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("exchange");
        sb.append('=');
        sb.append(((this.exchange == null)?"<null>":this.exchange));
        sb.append(',');
        sb.append("originalTradeID");
        sb.append('=');
        sb.append(((this.originalTradeID == null)?"<null>":this.originalTradeID));
        sb.append(',');
        sb.append("originalPrice");
        sb.append('=');
        sb.append(((this.originalPrice == null)?"<null>":this.originalPrice));
        sb.append(',');
        sb.append("originalSize");
        sb.append('=');
        sb.append(((this.originalSize == null)?"<null>":this.originalSize));
        sb.append(',');
        sb.append("originalConditions");
        sb.append('=');
        sb.append(((this.originalConditions == null)?"<null>":this.originalConditions));
        sb.append(',');
        sb.append("correctedTradeID");
        sb.append('=');
        sb.append(((this.correctedTradeID == null)?"<null>":this.correctedTradeID));
        sb.append(',');
        sb.append("correctedPrice");
        sb.append('=');
        sb.append(((this.correctedPrice == null)?"<null>":this.correctedPrice));
        sb.append(',');
        sb.append("correctedSize");
        sb.append('=');
        sb.append(((this.correctedSize == null)?"<null>":this.correctedSize));
        sb.append(',');
        sb.append("correctedConditions");
        sb.append('=');
        sb.append(((this.correctedConditions == null)?"<null>":this.correctedConditions));
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
        result = ((result* 31)+((this.correctedTradeID == null)? 0 :this.correctedTradeID.hashCode()));
        result = ((result* 31)+((this.symbol == null)? 0 :this.symbol.hashCode()));
        result = ((result* 31)+((this.originalPrice == null)? 0 :this.originalPrice.hashCode()));
        result = ((result* 31)+((this.correctedConditions == null)? 0 :this.correctedConditions.hashCode()));
        result = ((result* 31)+((this.tape == null)? 0 :this.tape.hashCode()));
        result = ((result* 31)+((this.correctedPrice == null)? 0 :this.correctedPrice.hashCode()));
        result = ((result* 31)+((this.exchange == null)? 0 :this.exchange.hashCode()));
        result = ((result* 31)+((this.originalSize == null)? 0 :this.originalSize.hashCode()));
        result = ((result* 31)+((this.correctedSize == null)? 0 :this.correctedSize.hashCode()));
        result = ((result* 31)+((this.originalTradeID == null)? 0 :this.originalTradeID.hashCode()));
        result = ((result* 31)+((this.originalConditions == null)? 0 :this.originalConditions.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StockTradeCorrectionMessage) == false) {
            return false;
        }
        StockTradeCorrectionMessage rhs = ((StockTradeCorrectionMessage) other);
        return ((((((((((((super.equals(rhs)&&((this.correctedTradeID == rhs.correctedTradeID)||((this.correctedTradeID!= null)&&this.correctedTradeID.equals(rhs.correctedTradeID))))&&((this.symbol == rhs.symbol)||((this.symbol!= null)&&this.symbol.equals(rhs.symbol))))&&((this.originalPrice == rhs.originalPrice)||((this.originalPrice!= null)&&this.originalPrice.equals(rhs.originalPrice))))&&((this.correctedConditions == rhs.correctedConditions)||((this.correctedConditions!= null)&&this.correctedConditions.equals(rhs.correctedConditions))))&&((this.tape == rhs.tape)||((this.tape!= null)&&this.tape.equals(rhs.tape))))&&((this.correctedPrice == rhs.correctedPrice)||((this.correctedPrice!= null)&&this.correctedPrice.equals(rhs.correctedPrice))))&&((this.exchange == rhs.exchange)||((this.exchange!= null)&&this.exchange.equals(rhs.exchange))))&&((this.originalSize == rhs.originalSize)||((this.originalSize!= null)&&this.originalSize.equals(rhs.originalSize))))&&((this.correctedSize == rhs.correctedSize)||((this.correctedSize!= null)&&this.correctedSize.equals(rhs.correctedSize))))&&((this.originalTradeID == rhs.originalTradeID)||((this.originalTradeID!= null)&&this.originalTradeID.equals(rhs.originalTradeID))))&&((this.originalConditions == rhs.originalConditions)||((this.originalConditions!= null)&&this.originalConditions.equals(rhs.originalConditions))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}
