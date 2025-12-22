
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.quote;

import java.io.Serializable;
import java.time.OffsetDateTime;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.CryptoMarketDataMessage;

public class CryptoQuoteMessage
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
    private Double askSize;
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
    private Double bidSize;
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
    private final static long serialVersionUID = 6245471857984690914L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CryptoQuoteMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public CryptoQuoteMessage(CryptoQuoteMessage source) {
        super();
        this.symbol = source.symbol;
        this.askPrice = source.askPrice;
        this.askSize = source.askSize;
        this.bidPrice = source.bidPrice;
        this.bidSize = source.bidSize;
        this.timestamp = source.timestamp;
    }

    /**
     * 
     * @param symbol
     *     The symbol.
     * @param askPrice
     *     The ask price.
     * @param bidSize
     *     The bid size.
     * @param askSize
     *     The ask size.
     * @param bidPrice
     *     The bid price.
     * @param timestamp
     *     The timestamp with nanosecond precision.
     */
    public CryptoQuoteMessage(String symbol, Double askPrice, Double askSize, Double bidPrice, Double bidSize, OffsetDateTime timestamp) {
        super();
        this.symbol = symbol;
        this.askPrice = askPrice;
        this.askSize = askSize;
        this.bidPrice = bidPrice;
        this.bidSize = bidSize;
        this.timestamp = timestamp;
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

    public CryptoQuoteMessage withSymbol(String symbol) {
        this.symbol = symbol;
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

    public CryptoQuoteMessage withAskPrice(Double askPrice) {
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
    public Double getAskSize() {
        return askSize;
    }

    /**
     * The ask size.
     * <p>
     * 
     * Corresponds to the "as" property.
     * 
     */
    public void setAskSize(Double askSize) {
        this.askSize = askSize;
    }

    public CryptoQuoteMessage withAskSize(Double askSize) {
        this.askSize = askSize;
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

    public CryptoQuoteMessage withBidPrice(Double bidPrice) {
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
    public Double getBidSize() {
        return bidSize;
    }

    /**
     * The bid size.
     * <p>
     * 
     * Corresponds to the "bs" property.
     * 
     */
    public void setBidSize(Double bidSize) {
        this.bidSize = bidSize;
    }

    public CryptoQuoteMessage withBidSize(Double bidSize) {
        this.bidSize = bidSize;
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

    public CryptoQuoteMessage withTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CryptoQuoteMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("askPrice");
        sb.append('=');
        sb.append(((this.askPrice == null)?"<null>":this.askPrice));
        sb.append(',');
        sb.append("askSize");
        sb.append('=');
        sb.append(((this.askSize == null)?"<null>":this.askSize));
        sb.append(',');
        sb.append("bidPrice");
        sb.append('=');
        sb.append(((this.bidPrice == null)?"<null>":this.bidPrice));
        sb.append(',');
        sb.append("bidSize");
        sb.append('=');
        sb.append(((this.bidSize == null)?"<null>":this.bidSize));
        sb.append(',');
        sb.append("timestamp");
        sb.append('=');
        sb.append(((this.timestamp == null)?"<null>":this.timestamp));
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
        result = ((result* 31)+((this.askSize == null)? 0 :this.askSize.hashCode()));
        result = ((result* 31)+((this.bidPrice == null)? 0 :this.bidPrice.hashCode()));
        result = ((result* 31)+((this.bidSize == null)? 0 :this.bidSize.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CryptoQuoteMessage) == false) {
            return false;
        }
        CryptoQuoteMessage rhs = ((CryptoQuoteMessage) other);
        return ((((((super.equals(rhs)&&((this.symbol == rhs.symbol)||((this.symbol!= null)&&this.symbol.equals(rhs.symbol))))&&((this.askPrice == rhs.askPrice)||((this.askPrice!= null)&&this.askPrice.equals(rhs.askPrice))))&&((this.askSize == rhs.askSize)||((this.askSize!= null)&&this.askSize.equals(rhs.askSize))))&&((this.bidPrice == rhs.bidPrice)||((this.bidPrice!= null)&&this.bidPrice.equals(rhs.bidPrice))))&&((this.bidSize == rhs.bidSize)||((this.bidSize!= null)&&this.bidSize.equals(rhs.bidSize))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}
