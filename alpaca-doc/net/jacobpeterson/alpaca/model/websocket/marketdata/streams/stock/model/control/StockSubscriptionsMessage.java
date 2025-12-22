
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.control;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.StockMarketDataMessage;

public class StockSubscriptionsMessage
    extends StockMarketDataMessage
    implements Serializable
{

    /**
     * The {@link java.util.Set} of symbols subscribed to trades.
     * <p>
     * 
     * 
     */
    @SerializedName("trades")
    @Expose
    private Set<String> trades = new LinkedHashSet<String>();
    /**
     * The {@link java.util.Set} of symbols subscribed to quotes.
     * <p>
     * 
     * 
     */
    @SerializedName("quotes")
    @Expose
    private Set<String> quotes = new LinkedHashSet<String>();
    /**
     * The {@link java.util.Set} of symbols subscribed to minute bars.
     * <p>
     * 
     * Corresponds to the "bars" property.
     * 
     */
    @SerializedName("bars")
    @Expose
    private Set<String> minuteBars = new LinkedHashSet<String>();
    /**
     * The {@link java.util.Set} of symbols subscribed to daily bars.
     * <p>
     * 
     * 
     */
    @SerializedName("dailyBars")
    @Expose
    private Set<String> dailyBars = new LinkedHashSet<String>();
    /**
     * The {@link java.util.Set} of symbols subscribed to updated bars.
     * <p>
     * 
     * 
     */
    @SerializedName("updatedBars")
    @Expose
    private Set<String> updatedBars = new LinkedHashSet<String>();
    /**
     * The {@link java.util.Set} of symbols subscribed to limit up - limit down bands.
     * <p>
     * 
     * Corresponds to the "lulds" property.
     * 
     */
    @SerializedName("lulds")
    @Expose
    private Set<String> limitUpLimitDownBands = new LinkedHashSet<String>();
    /**
     * The {@link java.util.Set} of symbols subscribed to trading statuses.
     * <p>
     * 
     * Corresponds to the "statuses" property.
     * 
     */
    @SerializedName("statuses")
    @Expose
    private Set<String> tradingStatuses = new LinkedHashSet<String>();
    private final static long serialVersionUID = 1620242801075376954L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public StockSubscriptionsMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public StockSubscriptionsMessage(StockSubscriptionsMessage source) {
        super();
        this.trades = source.trades;
        this.quotes = source.quotes;
        this.minuteBars = source.minuteBars;
        this.dailyBars = source.dailyBars;
        this.updatedBars = source.updatedBars;
        this.limitUpLimitDownBands = source.limitUpLimitDownBands;
        this.tradingStatuses = source.tradingStatuses;
    }

    /**
     * 
     * @param limitUpLimitDownBands
     *     The {@link java.util.Set} of symbols subscribed to limit up - limit down bands.
     * @param tradingStatuses
     *     The {@link java.util.Set} of symbols subscribed to trading statuses.
     * @param minuteBars
     *     The {@link java.util.Set} of symbols subscribed to minute bars.
     * @param dailyBars
     *     The {@link java.util.Set} of symbols subscribed to daily bars.
     * @param trades
     *     The {@link java.util.Set} of symbols subscribed to trades.
     * @param updatedBars
     *     The {@link java.util.Set} of symbols subscribed to updated bars.
     * @param quotes
     *     The {@link java.util.Set} of symbols subscribed to quotes.
     */
    public StockSubscriptionsMessage(Set<String> trades, Set<String> quotes, Set<String> minuteBars, Set<String> dailyBars, Set<String> updatedBars, Set<String> limitUpLimitDownBands, Set<String> tradingStatuses) {
        super();
        this.trades = trades;
        this.quotes = quotes;
        this.minuteBars = minuteBars;
        this.dailyBars = dailyBars;
        this.updatedBars = updatedBars;
        this.limitUpLimitDownBands = limitUpLimitDownBands;
        this.tradingStatuses = tradingStatuses;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to trades.
     * <p>
     * 
     * 
     */
    public Set<String> getTrades() {
        return trades;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to trades.
     * <p>
     * 
     * 
     */
    public void setTrades(Set<String> trades) {
        this.trades = trades;
    }

    public StockSubscriptionsMessage withTrades(Set<String> trades) {
        this.trades = trades;
        return this;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to quotes.
     * <p>
     * 
     * 
     */
    public Set<String> getQuotes() {
        return quotes;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to quotes.
     * <p>
     * 
     * 
     */
    public void setQuotes(Set<String> quotes) {
        this.quotes = quotes;
    }

    public StockSubscriptionsMessage withQuotes(Set<String> quotes) {
        this.quotes = quotes;
        return this;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to minute bars.
     * <p>
     * 
     * Corresponds to the "bars" property.
     * 
     */
    public Set<String> getMinuteBars() {
        return minuteBars;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to minute bars.
     * <p>
     * 
     * Corresponds to the "bars" property.
     * 
     */
    public void setMinuteBars(Set<String> minuteBars) {
        this.minuteBars = minuteBars;
    }

    public StockSubscriptionsMessage withMinuteBars(Set<String> minuteBars) {
        this.minuteBars = minuteBars;
        return this;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to daily bars.
     * <p>
     * 
     * 
     */
    public Set<String> getDailyBars() {
        return dailyBars;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to daily bars.
     * <p>
     * 
     * 
     */
    public void setDailyBars(Set<String> dailyBars) {
        this.dailyBars = dailyBars;
    }

    public StockSubscriptionsMessage withDailyBars(Set<String> dailyBars) {
        this.dailyBars = dailyBars;
        return this;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to updated bars.
     * <p>
     * 
     * 
     */
    public Set<String> getUpdatedBars() {
        return updatedBars;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to updated bars.
     * <p>
     * 
     * 
     */
    public void setUpdatedBars(Set<String> updatedBars) {
        this.updatedBars = updatedBars;
    }

    public StockSubscriptionsMessage withUpdatedBars(Set<String> updatedBars) {
        this.updatedBars = updatedBars;
        return this;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to limit up - limit down bands.
     * <p>
     * 
     * Corresponds to the "lulds" property.
     * 
     */
    public Set<String> getLimitUpLimitDownBands() {
        return limitUpLimitDownBands;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to limit up - limit down bands.
     * <p>
     * 
     * Corresponds to the "lulds" property.
     * 
     */
    public void setLimitUpLimitDownBands(Set<String> limitUpLimitDownBands) {
        this.limitUpLimitDownBands = limitUpLimitDownBands;
    }

    public StockSubscriptionsMessage withLimitUpLimitDownBands(Set<String> limitUpLimitDownBands) {
        this.limitUpLimitDownBands = limitUpLimitDownBands;
        return this;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to trading statuses.
     * <p>
     * 
     * Corresponds to the "statuses" property.
     * 
     */
    public Set<String> getTradingStatuses() {
        return tradingStatuses;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to trading statuses.
     * <p>
     * 
     * Corresponds to the "statuses" property.
     * 
     */
    public void setTradingStatuses(Set<String> tradingStatuses) {
        this.tradingStatuses = tradingStatuses;
    }

    public StockSubscriptionsMessage withTradingStatuses(Set<String> tradingStatuses) {
        this.tradingStatuses = tradingStatuses;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StockSubscriptionsMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("trades");
        sb.append('=');
        sb.append(((this.trades == null)?"<null>":this.trades));
        sb.append(',');
        sb.append("quotes");
        sb.append('=');
        sb.append(((this.quotes == null)?"<null>":this.quotes));
        sb.append(',');
        sb.append("minuteBars");
        sb.append('=');
        sb.append(((this.minuteBars == null)?"<null>":this.minuteBars));
        sb.append(',');
        sb.append("dailyBars");
        sb.append('=');
        sb.append(((this.dailyBars == null)?"<null>":this.dailyBars));
        sb.append(',');
        sb.append("updatedBars");
        sb.append('=');
        sb.append(((this.updatedBars == null)?"<null>":this.updatedBars));
        sb.append(',');
        sb.append("limitUpLimitDownBands");
        sb.append('=');
        sb.append(((this.limitUpLimitDownBands == null)?"<null>":this.limitUpLimitDownBands));
        sb.append(',');
        sb.append("tradingStatuses");
        sb.append('=');
        sb.append(((this.tradingStatuses == null)?"<null>":this.tradingStatuses));
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
        result = ((result* 31)+((this.limitUpLimitDownBands == null)? 0 :this.limitUpLimitDownBands.hashCode()));
        result = ((result* 31)+((this.tradingStatuses == null)? 0 :this.tradingStatuses.hashCode()));
        result = ((result* 31)+((this.minuteBars == null)? 0 :this.minuteBars.hashCode()));
        result = ((result* 31)+((this.dailyBars == null)? 0 :this.dailyBars.hashCode()));
        result = ((result* 31)+((this.trades == null)? 0 :this.trades.hashCode()));
        result = ((result* 31)+((this.updatedBars == null)? 0 :this.updatedBars.hashCode()));
        result = ((result* 31)+((this.quotes == null)? 0 :this.quotes.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StockSubscriptionsMessage) == false) {
            return false;
        }
        StockSubscriptionsMessage rhs = ((StockSubscriptionsMessage) other);
        return (((((((super.equals(rhs)&&((this.limitUpLimitDownBands == rhs.limitUpLimitDownBands)||((this.limitUpLimitDownBands!= null)&&this.limitUpLimitDownBands.equals(rhs.limitUpLimitDownBands))))&&((this.tradingStatuses == rhs.tradingStatuses)||((this.tradingStatuses!= null)&&this.tradingStatuses.equals(rhs.tradingStatuses))))&&((this.minuteBars == rhs.minuteBars)||((this.minuteBars!= null)&&this.minuteBars.equals(rhs.minuteBars))))&&((this.dailyBars == rhs.dailyBars)||((this.dailyBars!= null)&&this.dailyBars.equals(rhs.dailyBars))))&&((this.trades == rhs.trades)||((this.trades!= null)&&this.trades.equals(rhs.trades))))&&((this.updatedBars == rhs.updatedBars)||((this.updatedBars!= null)&&this.updatedBars.equals(rhs.updatedBars))))&&((this.quotes == rhs.quotes)||((this.quotes!= null)&&this.quotes.equals(rhs.quotes))));
    }

}
