
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.bar;

import java.io.Serializable;
import java.time.OffsetDateTime;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.StockMarketDataMessage;

public class StockBarMessage
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
     * The open price.
     * <p>
     * 
     * Corresponds to the "o" property.
     * 
     */
    @SerializedName("o")
    @Expose
    private Double open;
    /**
     * The high price.
     * <p>
     * 
     * Corresponds to the "h" property.
     * 
     */
    @SerializedName("h")
    @Expose
    private Double high;
    /**
     * The low price.
     * <p>
     * 
     * Corresponds to the "l" property.
     * 
     */
    @SerializedName("l")
    @Expose
    private Double low;
    /**
     * The close price.
     * <p>
     * 
     * Corresponds to the "c" property.
     * 
     */
    @SerializedName("c")
    @Expose
    private Double close;
    /**
     * The timestamp.
     * <p>
     * 
     * Corresponds to the "t" property.
     * 
     */
    @SerializedName("t")
    @Expose
    private OffsetDateTime timestamp;
    /**
     * The volume.
     * <p>
     * 
     * Corresponds to the "v" property.
     * 
     */
    @SerializedName("v")
    @Expose
    private Long volume;
    /**
     * The trade count.
     * <p>
     * 
     * Corresponds to the "n" property.
     * 
     */
    @SerializedName("n")
    @Expose
    private Long tradeCount;
    /**
     * The VWAP (Volume Weighted Average Price).
     * <p>
     * 
     * Corresponds to the "vw" property.
     * 
     */
    @SerializedName("vw")
    @Expose
    private Double vwap;
    private final static long serialVersionUID = 7614837406792177020L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public StockBarMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public StockBarMessage(StockBarMessage source) {
        super();
        this.symbol = source.symbol;
        this.open = source.open;
        this.high = source.high;
        this.low = source.low;
        this.close = source.close;
        this.timestamp = source.timestamp;
        this.volume = source.volume;
        this.tradeCount = source.tradeCount;
        this.vwap = source.vwap;
    }

    /**
     * 
     * @param volume
     *     The volume.
     * @param symbol
     *     The symbol.
     * @param high
     *     The high price.
     * @param tradeCount
     *     The trade count.
     * @param low
     *     The low price.
     * @param vwap
     *     The VWAP (Volume Weighted Average Price).
     * @param close
     *     The close price.
     * @param open
     *     The open price.
     * @param timestamp
     *     The timestamp.
     */
    public StockBarMessage(String symbol, Double open, Double high, Double low, Double close, OffsetDateTime timestamp, Long volume, Long tradeCount, Double vwap) {
        super();
        this.symbol = symbol;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.timestamp = timestamp;
        this.volume = volume;
        this.tradeCount = tradeCount;
        this.vwap = vwap;
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

    public StockBarMessage withSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    /**
     * The open price.
     * <p>
     * 
     * Corresponds to the "o" property.
     * 
     */
    public Double getOpen() {
        return open;
    }

    /**
     * The open price.
     * <p>
     * 
     * Corresponds to the "o" property.
     * 
     */
    public void setOpen(Double open) {
        this.open = open;
    }

    public StockBarMessage withOpen(Double open) {
        this.open = open;
        return this;
    }

    /**
     * The high price.
     * <p>
     * 
     * Corresponds to the "h" property.
     * 
     */
    public Double getHigh() {
        return high;
    }

    /**
     * The high price.
     * <p>
     * 
     * Corresponds to the "h" property.
     * 
     */
    public void setHigh(Double high) {
        this.high = high;
    }

    public StockBarMessage withHigh(Double high) {
        this.high = high;
        return this;
    }

    /**
     * The low price.
     * <p>
     * 
     * Corresponds to the "l" property.
     * 
     */
    public Double getLow() {
        return low;
    }

    /**
     * The low price.
     * <p>
     * 
     * Corresponds to the "l" property.
     * 
     */
    public void setLow(Double low) {
        this.low = low;
    }

    public StockBarMessage withLow(Double low) {
        this.low = low;
        return this;
    }

    /**
     * The close price.
     * <p>
     * 
     * Corresponds to the "c" property.
     * 
     */
    public Double getClose() {
        return close;
    }

    /**
     * The close price.
     * <p>
     * 
     * Corresponds to the "c" property.
     * 
     */
    public void setClose(Double close) {
        this.close = close;
    }

    public StockBarMessage withClose(Double close) {
        this.close = close;
        return this;
    }

    /**
     * The timestamp.
     * <p>
     * 
     * Corresponds to the "t" property.
     * 
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * The timestamp.
     * <p>
     * 
     * Corresponds to the "t" property.
     * 
     */
    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public StockBarMessage withTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * The volume.
     * <p>
     * 
     * Corresponds to the "v" property.
     * 
     */
    public Long getVolume() {
        return volume;
    }

    /**
     * The volume.
     * <p>
     * 
     * Corresponds to the "v" property.
     * 
     */
    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public StockBarMessage withVolume(Long volume) {
        this.volume = volume;
        return this;
    }

    /**
     * The trade count.
     * <p>
     * 
     * Corresponds to the "n" property.
     * 
     */
    public Long getTradeCount() {
        return tradeCount;
    }

    /**
     * The trade count.
     * <p>
     * 
     * Corresponds to the "n" property.
     * 
     */
    public void setTradeCount(Long tradeCount) {
        this.tradeCount = tradeCount;
    }

    public StockBarMessage withTradeCount(Long tradeCount) {
        this.tradeCount = tradeCount;
        return this;
    }

    /**
     * The VWAP (Volume Weighted Average Price).
     * <p>
     * 
     * Corresponds to the "vw" property.
     * 
     */
    public Double getVwap() {
        return vwap;
    }

    /**
     * The VWAP (Volume Weighted Average Price).
     * <p>
     * 
     * Corresponds to the "vw" property.
     * 
     */
    public void setVwap(Double vwap) {
        this.vwap = vwap;
    }

    public StockBarMessage withVwap(Double vwap) {
        this.vwap = vwap;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StockBarMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("open");
        sb.append('=');
        sb.append(((this.open == null)?"<null>":this.open));
        sb.append(',');
        sb.append("high");
        sb.append('=');
        sb.append(((this.high == null)?"<null>":this.high));
        sb.append(',');
        sb.append("low");
        sb.append('=');
        sb.append(((this.low == null)?"<null>":this.low));
        sb.append(',');
        sb.append("close");
        sb.append('=');
        sb.append(((this.close == null)?"<null>":this.close));
        sb.append(',');
        sb.append("timestamp");
        sb.append('=');
        sb.append(((this.timestamp == null)?"<null>":this.timestamp));
        sb.append(',');
        sb.append("volume");
        sb.append('=');
        sb.append(((this.volume == null)?"<null>":this.volume));
        sb.append(',');
        sb.append("tradeCount");
        sb.append('=');
        sb.append(((this.tradeCount == null)?"<null>":this.tradeCount));
        sb.append(',');
        sb.append("vwap");
        sb.append('=');
        sb.append(((this.vwap == null)?"<null>":this.vwap));
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
        result = ((result* 31)+((this.volume == null)? 0 :this.volume.hashCode()));
        result = ((result* 31)+((this.symbol == null)? 0 :this.symbol.hashCode()));
        result = ((result* 31)+((this.high == null)? 0 :this.high.hashCode()));
        result = ((result* 31)+((this.tradeCount == null)? 0 :this.tradeCount.hashCode()));
        result = ((result* 31)+((this.low == null)? 0 :this.low.hashCode()));
        result = ((result* 31)+((this.vwap == null)? 0 :this.vwap.hashCode()));
        result = ((result* 31)+((this.close == null)? 0 :this.close.hashCode()));
        result = ((result* 31)+((this.open == null)? 0 :this.open.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StockBarMessage) == false) {
            return false;
        }
        StockBarMessage rhs = ((StockBarMessage) other);
        return (((((((((super.equals(rhs)&&((this.volume == rhs.volume)||((this.volume!= null)&&this.volume.equals(rhs.volume))))&&((this.symbol == rhs.symbol)||((this.symbol!= null)&&this.symbol.equals(rhs.symbol))))&&((this.high == rhs.high)||((this.high!= null)&&this.high.equals(rhs.high))))&&((this.tradeCount == rhs.tradeCount)||((this.tradeCount!= null)&&this.tradeCount.equals(rhs.tradeCount))))&&((this.low == rhs.low)||((this.low!= null)&&this.low.equals(rhs.low))))&&((this.vwap == rhs.vwap)||((this.vwap!= null)&&this.vwap.equals(rhs.vwap))))&&((this.close == rhs.close)||((this.close!= null)&&this.close.equals(rhs.close))))&&((this.open == rhs.open)||((this.open!= null)&&this.open.equals(rhs.open))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}
