
package net.jacobpeterson.alpaca.model.websocket.updates.model.listening;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.updates.model.UpdatesMessageType;

public class ListeningData implements Serializable
{

    /**
     * The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.updates.model.UpdatesMessageType}s.
     * <p>
     * 
     * 
     */
    @SerializedName("streams")
    @Expose
    private Set<UpdatesMessageType> streams = new LinkedHashSet<UpdatesMessageType>();
    private final static long serialVersionUID = 2997105761244967961L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ListeningData() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public ListeningData(ListeningData source) {
        super();
        this.streams = source.streams;
    }

    /**
     * 
     * @param streams
     *     The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.updates.model.UpdatesMessageType}s.
     */
    public ListeningData(Set<UpdatesMessageType> streams) {
        super();
        this.streams = streams;
    }

    /**
     * The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.updates.model.UpdatesMessageType}s.
     * <p>
     * 
     * 
     */
    public Set<UpdatesMessageType> getStreams() {
        return streams;
    }

    /**
     * The {@link java.util.Set} of {@link net.jacobpeterson.alpaca.model.websocket.updates.model.UpdatesMessageType}s.
     * <p>
     * 
     * 
     */
    public void setStreams(Set<UpdatesMessageType> streams) {
        this.streams = streams;
    }

    public ListeningData withStreams(Set<UpdatesMessageType> streams) {
        this.streams = streams;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ListeningData.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("streams");
        sb.append('=');
        sb.append(((this.streams == null)?"<null>":this.streams));
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
        result = ((result* 31)+((this.streams == null)? 0 :this.streams.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ListeningData) == false) {
            return false;
        }
        ListeningData rhs = ((ListeningData) other);
        return ((this.streams == rhs.streams)||((this.streams!= null)&&this.streams.equals(rhs.streams)));
    }

}
