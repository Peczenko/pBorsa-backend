
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.limituplimitdownband;

import java.io.Serializable;
import java.time.OffsetDateTime;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.StockMarketDataMessage;

public class StockLimitUpLimitDownBandMessage
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
    private String symbol;
    /**
     * The upper limit price band of a security.
     * <p>
     * 
     * Corresponds to the "u" property.
     * 
     */
    @SerializedName("u")
    @Expose
    private Double limitUpPrice;
    /**
     * The lower limit price band of a security.
     * <p>
     * 
     * Corresponds to the "d" property.
     * 
     */
    @SerializedName("d")
    @Expose
    private Double limitDownPrice;
    /**
     * The indicator.
     * <p>
     * 
     * Corresponds to the "i" property.
     * 
     */
    @SerializedName("i")
    @Expose
    private String indicator;
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
    private String tape;
    private final static long serialVersionUID = -3505975037860803753L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public StockLimitUpLimitDownBandMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public StockLimitUpLimitDownBandMessage(StockLimitUpLimitDownBandMessage source) {
        super();
        this.symbol = source.symbol;
        this.limitUpPrice = source.limitUpPrice;
        this.limitDownPrice = source.limitDownPrice;
        this.indicator = source.indicator;
        this.timestamp = source.timestamp;
        this.tape = source.tape;
    }

    /**
     * 
     * @param indicator
     *     The indicator.
     * @param symbol
     *     The symbol.
     * @param limitDownPrice
     *     The lower limit price band of a security.
     * @param tape
     *     The tape.
     * @param limitUpPrice
     *     The upper limit price band of a security.
     * @param timestamp
     *     The timestamp with nanosecond precision.
     */
    public StockLimitUpLimitDownBandMessage(String symbol, Double limitUpPrice, Double limitDownPrice, String indicator, OffsetDateTime timestamp, String tape) {
        super();
        this.symbol = symbol;
        this.limitUpPrice = limitUpPrice;
        this.limitDownPrice = limitDownPrice;
        this.indicator = indicator;
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

    public StockLimitUpLimitDownBandMessage withSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    /**
     * The upper limit price band of a security.
     * <p>
     * 
     * Corresponds to the "u" property.
     * 
     */
    public Double getLimitUpPrice() {
        return limitUpPrice;
    }

    /**
     * The upper limit price band of a security.
     * <p>
     * 
     * Corresponds to the "u" property.
     * 
     */
    public void setLimitUpPrice(Double limitUpPrice) {
        this.limitUpPrice = limitUpPrice;
    }

    public StockLimitUpLimitDownBandMessage withLimitUpPrice(Double limitUpPrice) {
        this.limitUpPrice = limitUpPrice;
        return this;
    }

    /**
     * The lower limit price band of a security.
     * <p>
     * 
     * Corresponds to the "d" property.
     * 
     */
    public Double getLimitDownPrice() {
        return limitDownPrice;
    }

    /**
     * The lower limit price band of a security.
     * <p>
     * 
     * Corresponds to the "d" property.
     * 
     */
    public void setLimitDownPrice(Double limitDownPrice) {
        this.limitDownPrice = limitDownPrice;
    }

    public StockLimitUpLimitDownBandMessage withLimitDownPrice(Double limitDownPrice) {
        this.limitDownPrice = limitDownPrice;
        return this;
    }

    /**
     * The indicator.
     * <p>
     * 
     * Corresponds to the "i" property.
     * 
     */
    public String getIndicator() {
        return indicator;
    }

    /**
     * The indicator.
     * <p>
     * 
     * Corresponds to the "i" property.
     * 
     */
    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public StockLimitUpLimitDownBandMessage withIndicator(String indicator) {
        this.indicator = indicator;
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

    public StockLimitUpLimitDownBandMessage withTimestamp(OffsetDateTime timestamp) {
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
    public String getTape() {
        return tape;
    }

    /**
     * The tape.
     * <p>
     * 
     * Corresponds to the "z" property.
     * 
     */
    public void setTape(String tape) {
        this.tape = tape;
    }

    public StockLimitUpLimitDownBandMessage withTape(String tape) {
        this.tape = tape;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StockLimitUpLimitDownBandMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("limitUpPrice");
        sb.append('=');
        sb.append(((this.limitUpPrice == null)?"<null>":this.limitUpPrice));
        sb.append(',');
        sb.append("limitDownPrice");
        sb.append('=');
        sb.append(((this.limitDownPrice == null)?"<null>":this.limitDownPrice));
        sb.append(',');
        sb.append("indicator");
        sb.append('=');
        sb.append(((this.indicator == null)?"<null>":this.indicator));
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
        result = ((result* 31)+((this.limitUpPrice == null)? 0 :this.limitUpPrice.hashCode()));
        result = ((result* 31)+((this.indicator == null)? 0 :this.indicator.hashCode()));
        result = ((result* 31)+((this.symbol == null)? 0 :this.symbol.hashCode()));
        result = ((result* 31)+((this.limitDownPrice == null)? 0 :this.limitDownPrice.hashCode()));
        result = ((result* 31)+((this.tape == null)? 0 :this.tape.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StockLimitUpLimitDownBandMessage) == false) {
            return false;
        }
        StockLimitUpLimitDownBandMessage rhs = ((StockLimitUpLimitDownBandMessage) other);
        return ((((((super.equals(rhs)&&((this.limitUpPrice == rhs.limitUpPrice)||((this.limitUpPrice!= null)&&this.limitUpPrice.equals(rhs.limitUpPrice))))&&((this.indicator == rhs.indicator)||((this.indicator!= null)&&this.indicator.equals(rhs.indicator))))&&((this.symbol == rhs.symbol)||((this.symbol!= null)&&this.symbol.equals(rhs.symbol))))&&((this.limitDownPrice == rhs.limitDownPrice)||((this.limitDownPrice!= null)&&this.limitDownPrice.equals(rhs.limitDownPrice))))&&((this.tape == rhs.tape)||((this.tape!= null)&&this.tape.equals(rhs.tape))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}
