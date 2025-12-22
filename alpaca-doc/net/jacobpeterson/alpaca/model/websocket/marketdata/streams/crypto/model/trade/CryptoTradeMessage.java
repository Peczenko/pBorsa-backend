
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.trade;

import java.io.Serializable;
import java.time.OffsetDateTime;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.CryptoMarketDataMessage;

public class CryptoTradeMessage
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
    private Double size;
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
     * The taker side.
     * <p>
     * 
     * Corresponds to the "tks" property.
     * 
     */
    @SerializedName("tks")
    @Expose
    private CryptoTradeTakerSide takerSide;
    private final static long serialVersionUID = -8557692800315511807L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CryptoTradeMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public CryptoTradeMessage(CryptoTradeMessage source) {
        super();
        this.symbol = source.symbol;
        this.tradeID = source.tradeID;
        this.price = source.price;
        this.size = source.size;
        this.timestamp = source.timestamp;
        this.takerSide = source.takerSide;
    }

    /**
     * 
     * @param symbol
     *     The symbol.
     * @param size
     *     The trade size.
     * @param takerSide
     *     The taker side.
     * @param price
     *     The trade price.
     * @param tradeID
     *     The trade ID.
     * @param timestamp
     *     The timestamp with nanosecond precision.
     */
    public CryptoTradeMessage(String symbol, Long tradeID, Double price, Double size, OffsetDateTime timestamp, CryptoTradeTakerSide takerSide) {
        super();
        this.symbol = symbol;
        this.tradeID = tradeID;
        this.price = price;
        this.size = size;
        this.timestamp = timestamp;
        this.takerSide = takerSide;
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

    public CryptoTradeMessage withSymbol(String symbol) {
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

    public CryptoTradeMessage withTradeID(Long tradeID) {
        this.tradeID = tradeID;
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

    public CryptoTradeMessage withPrice(Double price) {
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
    public Double getSize() {
        return size;
    }

    /**
     * The trade size.
     * <p>
     * 
     * Corresponds to the "s" property.
     * 
     */
    public void setSize(Double size) {
        this.size = size;
    }

    public CryptoTradeMessage withSize(Double size) {
        this.size = size;
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

    public CryptoTradeMessage withTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * The taker side.
     * <p>
     * 
     * Corresponds to the "tks" property.
     * 
     */
    public CryptoTradeTakerSide getTakerSide() {
        return takerSide;
    }

    /**
     * The taker side.
     * <p>
     * 
     * Corresponds to the "tks" property.
     * 
     */
    public void setTakerSide(CryptoTradeTakerSide takerSide) {
        this.takerSide = takerSide;
    }

    public CryptoTradeMessage withTakerSide(CryptoTradeTakerSide takerSide) {
        this.takerSide = takerSide;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CryptoTradeMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("tradeID");
        sb.append('=');
        sb.append(((this.tradeID == null)?"<null>":this.tradeID));
        sb.append(',');
        sb.append("price");
        sb.append('=');
        sb.append(((this.price == null)?"<null>":this.price));
        sb.append(',');
        sb.append("size");
        sb.append('=');
        sb.append(((this.size == null)?"<null>":this.size));
        sb.append(',');
        sb.append("timestamp");
        sb.append('=');
        sb.append(((this.timestamp == null)?"<null>":this.timestamp));
        sb.append(',');
        sb.append("takerSide");
        sb.append('=');
        sb.append(((this.takerSide == null)?"<null>":this.takerSide));
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
        result = ((result* 31)+((this.takerSide == null)? 0 :this.takerSide.hashCode()));
        result = ((result* 31)+((this.tradeID == null)? 0 :this.tradeID.hashCode()));
        result = ((result* 31)+((this.price == null)? 0 :this.price.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CryptoTradeMessage) == false) {
            return false;
        }
        CryptoTradeMessage rhs = ((CryptoTradeMessage) other);
        return ((((((super.equals(rhs)&&((this.symbol == rhs.symbol)||((this.symbol!= null)&&this.symbol.equals(rhs.symbol))))&&((this.size == rhs.size)||((this.size!= null)&&this.size.equals(rhs.size))))&&((this.takerSide == rhs.takerSide)||((this.takerSide!= null)&&this.takerSide.equals(rhs.takerSide))))&&((this.tradeID == rhs.tradeID)||((this.tradeID!= null)&&this.tradeID.equals(rhs.tradeID))))&&((this.price == rhs.price)||((this.price!= null)&&this.price.equals(rhs.price))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}
