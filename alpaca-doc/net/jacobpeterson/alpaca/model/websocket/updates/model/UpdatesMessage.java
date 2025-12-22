
package net.jacobpeterson.alpaca.model.websocket.updates.model;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdatesMessage implements Serializable
{

    /**
     * The {@link net.jacobpeterson.alpaca.model.websocket.updates.model.UpdatesMessageType}.
     * <p>
     * 
     * 
     */
    @SerializedName("stream")
    @Expose
    private UpdatesMessageType stream;
    private final static long serialVersionUID = -7706232410106910163L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public UpdatesMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public UpdatesMessage(UpdatesMessage source) {
        super();
        this.stream = source.stream;
    }

    /**
     * 
     * @param stream
     *     The {@link net.jacobpeterson.alpaca.model.websocket.updates.model.UpdatesMessageType}.
     */
    public UpdatesMessage(UpdatesMessageType stream) {
        super();
        this.stream = stream;
    }

    /**
     * The {@link net.jacobpeterson.alpaca.model.websocket.updates.model.UpdatesMessageType}.
     * <p>
     * 
     * 
     */
    public UpdatesMessageType getStream() {
        return stream;
    }

    /**
     * The {@link net.jacobpeterson.alpaca.model.websocket.updates.model.UpdatesMessageType}.
     * <p>
     * 
     * 
     */
    public void setStream(UpdatesMessageType stream) {
        this.stream = stream;
    }

    public UpdatesMessage withStream(UpdatesMessageType stream) {
        this.stream = stream;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(UpdatesMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("stream");
        sb.append('=');
        sb.append(((this.stream == null)?"<null>":this.stream));
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
        result = ((result* 31)+((this.stream == null)? 0 :this.stream.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UpdatesMessage) == false) {
            return false;
        }
        UpdatesMessage rhs = ((UpdatesMessage) other);
        return ((this.stream == rhs.stream)||((this.stream!= null)&&this.stream.equals(rhs.stream)));
    }

}
