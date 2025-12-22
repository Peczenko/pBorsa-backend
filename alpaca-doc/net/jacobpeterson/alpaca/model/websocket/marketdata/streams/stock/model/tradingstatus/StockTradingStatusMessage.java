
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.tradingstatus;

import java.io.Serializable;
import java.time.OffsetDateTime;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.StockMarketDataMessage;

public class StockTradingStatusMessage
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
     * The status code.
     * <p>
     * 
     * Corresponds to the "sc" property.
     * 
     */
    @SerializedName("sc")
    @Expose
    private String statusCode;
    /**
     * The status message.
     * <p>
     * 
     * Corresponds to the "sm" property.
     * 
     */
    @SerializedName("sm")
    @Expose
    private String statusMessage;
    /**
     * The reason code.
     * <p>
     * 
     * Corresponds to the "rc" property.
     * 
     */
    @SerializedName("rc")
    @Expose
    private String reasonCode;
    /**
     * The reason message.
     * <p>
     * 
     * Corresponds to the "rm" property.
     * 
     */
    @SerializedName("rm")
    @Expose
    private String reasonMessage;
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
    private final static long serialVersionUID = 959318093529481076L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public StockTradingStatusMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public StockTradingStatusMessage(StockTradingStatusMessage source) {
        super();
        this.symbol = source.symbol;
        this.statusCode = source.statusCode;
        this.statusMessage = source.statusMessage;
        this.reasonCode = source.reasonCode;
        this.reasonMessage = source.reasonMessage;
        this.timestamp = source.timestamp;
        this.tape = source.tape;
    }

    /**
     * 
     * @param symbol
     *     The symbol.
     * @param tape
     *     The tape.
     * @param reasonMessage
     *     The reason message.
     * @param reasonCode
     *     The reason code.
     * @param statusMessage
     *     The status message.
     * @param statusCode
     *     The status code.
     * @param timestamp
     *     The timestamp with nanosecond precision.
     */
    public StockTradingStatusMessage(String symbol, String statusCode, String statusMessage, String reasonCode, String reasonMessage, OffsetDateTime timestamp, String tape) {
        super();
        this.symbol = symbol;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.reasonCode = reasonCode;
        this.reasonMessage = reasonMessage;
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

    public StockTradingStatusMessage withSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    /**
     * The status code.
     * <p>
     * 
     * Corresponds to the "sc" property.
     * 
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * The status code.
     * <p>
     * 
     * Corresponds to the "sc" property.
     * 
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public StockTradingStatusMessage withStatusCode(String statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * The status message.
     * <p>
     * 
     * Corresponds to the "sm" property.
     * 
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * The status message.
     * <p>
     * 
     * Corresponds to the "sm" property.
     * 
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public StockTradingStatusMessage withStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    /**
     * The reason code.
     * <p>
     * 
     * Corresponds to the "rc" property.
     * 
     */
    public String getReasonCode() {
        return reasonCode;
    }

    /**
     * The reason code.
     * <p>
     * 
     * Corresponds to the "rc" property.
     * 
     */
    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public StockTradingStatusMessage withReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    /**
     * The reason message.
     * <p>
     * 
     * Corresponds to the "rm" property.
     * 
     */
    public String getReasonMessage() {
        return reasonMessage;
    }

    /**
     * The reason message.
     * <p>
     * 
     * Corresponds to the "rm" property.
     * 
     */
    public void setReasonMessage(String reasonMessage) {
        this.reasonMessage = reasonMessage;
    }

    public StockTradingStatusMessage withReasonMessage(String reasonMessage) {
        this.reasonMessage = reasonMessage;
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

    public StockTradingStatusMessage withTimestamp(OffsetDateTime timestamp) {
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

    public StockTradingStatusMessage withTape(String tape) {
        this.tape = tape;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StockTradingStatusMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("statusCode");
        sb.append('=');
        sb.append(((this.statusCode == null)?"<null>":this.statusCode));
        sb.append(',');
        sb.append("statusMessage");
        sb.append('=');
        sb.append(((this.statusMessage == null)?"<null>":this.statusMessage));
        sb.append(',');
        sb.append("reasonCode");
        sb.append('=');
        sb.append(((this.reasonCode == null)?"<null>":this.reasonCode));
        sb.append(',');
        sb.append("reasonMessage");
        sb.append('=');
        sb.append(((this.reasonMessage == null)?"<null>":this.reasonMessage));
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
        result = ((result* 31)+((this.tape == null)? 0 :this.tape.hashCode()));
        result = ((result* 31)+((this.reasonMessage == null)? 0 :this.reasonMessage.hashCode()));
        result = ((result* 31)+((this.reasonCode == null)? 0 :this.reasonCode.hashCode()));
        result = ((result* 31)+((this.statusMessage == null)? 0 :this.statusMessage.hashCode()));
        result = ((result* 31)+((this.statusCode == null)? 0 :this.statusCode.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StockTradingStatusMessage) == false) {
            return false;
        }
        StockTradingStatusMessage rhs = ((StockTradingStatusMessage) other);
        return (((((((super.equals(rhs)&&((this.symbol == rhs.symbol)||((this.symbol!= null)&&this.symbol.equals(rhs.symbol))))&&((this.tape == rhs.tape)||((this.tape!= null)&&this.tape.equals(rhs.tape))))&&((this.reasonMessage == rhs.reasonMessage)||((this.reasonMessage!= null)&&this.reasonMessage.equals(rhs.reasonMessage))))&&((this.reasonCode == rhs.reasonCode)||((this.reasonCode!= null)&&this.reasonCode.equals(rhs.reasonCode))))&&((this.statusMessage == rhs.statusMessage)||((this.statusMessage!= null)&&this.statusMessage.equals(rhs.statusMessage))))&&((this.statusCode == rhs.statusCode)||((this.statusCode!= null)&&this.statusCode.equals(rhs.statusCode))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}
