
package net.jacobpeterson.alpaca.model.websocket.updates.model.authorization;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthorizationData implements Serializable
{

    /**
     * The status.
     * <p>
     * 
     * 
     */
    @SerializedName("status")
    @Expose
    private String status;
    /**
     * The desired action.
     * <p>
     * 
     * 
     */
    @SerializedName("action")
    @Expose
    private String action;
    private final static long serialVersionUID = 4019205706633291487L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AuthorizationData() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public AuthorizationData(AuthorizationData source) {
        super();
        this.status = source.status;
        this.action = source.action;
    }

    /**
     * 
     * @param action
     *     The desired action.
     * @param status
     *     The status.
     */
    public AuthorizationData(String status, String action) {
        super();
        this.status = status;
        this.action = action;
    }

    /**
     * The status.
     * <p>
     * 
     * 
     */
    public String getStatus() {
        return status;
    }

    /**
     * The status.
     * <p>
     * 
     * 
     */
    public void setStatus(String status) {
        this.status = status;
    }

    public AuthorizationData withStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * The desired action.
     * <p>
     * 
     * 
     */
    public String getAction() {
        return action;
    }

    /**
     * The desired action.
     * <p>
     * 
     * 
     */
    public void setAction(String action) {
        this.action = action;
    }

    public AuthorizationData withAction(String action) {
        this.action = action;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AuthorizationData.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
        sb.append(',');
        sb.append("action");
        sb.append('=');
        sb.append(((this.action == null)?"<null>":this.action));
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
        result = ((result* 31)+((this.action == null)? 0 :this.action.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AuthorizationData) == false) {
            return false;
        }
        AuthorizationData rhs = ((AuthorizationData) other);
        return (((this.action == rhs.action)||((this.action!= null)&&this.action.equals(rhs.action)))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }

}
