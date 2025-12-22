
package net.jacobpeterson.alpaca.model.websocket.marketdata.model.control;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.model.MarketDataMessage;

public class SuccessMessage
    extends MarketDataMessage
    implements Serializable
{

    /**
     * The success message type.
     * <p>
     * 
     * Corresponds to the "msg" property.
     * 
     */
    @SerializedName("msg")
    @Expose
    private SuccessMessageType messageType;
    private final static long serialVersionUID = 5379081732292364952L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SuccessMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public SuccessMessage(SuccessMessage source) {
        super();
        this.messageType = source.messageType;
    }

    /**
     * 
     * @param messageType
     *     The success message type.
     */
    public SuccessMessage(SuccessMessageType messageType) {
        super();
        this.messageType = messageType;
    }

    /**
     * The success message type.
     * <p>
     * 
     * Corresponds to the "msg" property.
     * 
     */
    public SuccessMessageType getMessageType() {
        return messageType;
    }

    /**
     * The success message type.
     * <p>
     * 
     * Corresponds to the "msg" property.
     * 
     */
    public void setMessageType(SuccessMessageType messageType) {
        this.messageType = messageType;
    }

    public SuccessMessage withMessageType(SuccessMessageType messageType) {
        this.messageType = messageType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SuccessMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof SuccessMessage) == false) {
            return false;
        }
        SuccessMessage rhs = ((SuccessMessage) other);
        return (super.equals(rhs)&&((this.messageType == rhs.messageType)||((this.messageType!= null)&&this.messageType.equals(rhs.messageType))));
    }

}
