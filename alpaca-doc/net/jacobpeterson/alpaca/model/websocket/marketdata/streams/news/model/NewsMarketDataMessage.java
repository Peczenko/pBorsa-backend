
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.news.model;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.model.MarketDataMessage;

public class NewsMarketDataMessage
    extends MarketDataMessage
    implements Serializable
{

    /**
     * The {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.news.model.NewsMarketDataMessageType}.
     * <p>
     * 
     * Corresponds to the "T" property.
     * 
     */
    @SerializedName("T")
    @Expose
    private NewsMarketDataMessageType messageType;
    private final static long serialVersionUID = 8071555944830536271L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public NewsMarketDataMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public NewsMarketDataMessage(NewsMarketDataMessage source) {
        super();
        this.messageType = source.messageType;
    }

    /**
     * 
     * @param messageType
     *     The {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.news.model.NewsMarketDataMessageType}.
     */
    public NewsMarketDataMessage(NewsMarketDataMessageType messageType) {
        super();
        this.messageType = messageType;
    }

    /**
     * The {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.news.model.NewsMarketDataMessageType}.
     * <p>
     * 
     * Corresponds to the "T" property.
     * 
     */
    public NewsMarketDataMessageType getMessageType() {
        return messageType;
    }

    /**
     * The {@link net.jacobpeterson.alpaca.model.websocket.marketdata.streams.news.model.NewsMarketDataMessageType}.
     * <p>
     * 
     * Corresponds to the "T" property.
     * 
     */
    public void setMessageType(NewsMarketDataMessageType messageType) {
        this.messageType = messageType;
    }

    public NewsMarketDataMessage withMessageType(NewsMarketDataMessageType messageType) {
        this.messageType = messageType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(NewsMarketDataMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("messageType");
        sb.append('=');
        sb.append(((this.messageType == null)?"<null>":this.messageType));
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
        result = ((result* 31)+((this.messageType == null)? 0 :this.messageType.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NewsMarketDataMessage) == false) {
            return false;
        }
        NewsMarketDataMessage rhs = ((NewsMarketDataMessage) other);
        return (super.equals(rhs)&&((this.messageType == rhs.messageType)||((this.messageType!= null)&&this.messageType.equals(rhs.messageType))));
    }

}
