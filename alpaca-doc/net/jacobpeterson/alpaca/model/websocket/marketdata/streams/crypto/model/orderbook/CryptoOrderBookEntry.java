
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.orderbook;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CryptoOrderBookEntry implements Serializable
{

    /**
     * The price.
     * <p>
     * 
     * Corresponds to the "p" property.
     * 
     */
    @SerializedName("p")
    @Expose
    private Double price;
    /**
     * The size.
     * <p>
     * 
     * Corresponds to the "s" property.
     * 
     */
    @SerializedName("s")
    @Expose
    private Double size;
    private final static long serialVersionUID = 5384779288238676344L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CryptoOrderBookEntry() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public CryptoOrderBookEntry(CryptoOrderBookEntry source) {
        super();
        this.price = source.price;
        this.size = source.size;
    }

    /**
     * 
     * @param size
     *     The size.
     * @param price
     *     The price.
     */
    public CryptoOrderBookEntry(Double price, Double size) {
        super();
        this.price = price;
        this.size = size;
    }

    /**
     * The price.
     * <p>
     * 
     * Corresponds to the "p" property.
     * 
     */
    public Double getPrice() {
        return price;
    }

    /**
     * The price.
     * <p>
     * 
     * Corresponds to the "p" property.
     * 
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    public CryptoOrderBookEntry withPrice(Double price) {
        this.price = price;
        return this;
    }

    /**
     * The size.
     * <p>
     * 
     * Corresponds to the "s" property.
     * 
     */
    public Double getSize() {
        return size;
    }

    /**
     * The size.
     * <p>
     * 
     * Corresponds to the "s" property.
     * 
     */
    public void setSize(Double size) {
        this.size = size;
    }

    public CryptoOrderBookEntry withSize(Double size) {
        this.size = size;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CryptoOrderBookEntry.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("price");
        sb.append('=');
        sb.append(((this.price == null)?"<null>":this.price));
        sb.append(',');
        sb.append("size");
        sb.append('=');
        sb.append(((this.size == null)?"<null>":this.size));
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
        result = ((result* 31)+((this.size == null)? 0 :this.size.hashCode()));
        result = ((result* 31)+((this.price == null)? 0 :this.price.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CryptoOrderBookEntry) == false) {
            return false;
        }
        CryptoOrderBookEntry rhs = ((CryptoOrderBookEntry) other);
        return (((this.size == rhs.size)||((this.size!= null)&&this.size.equals(rhs.size)))&&((this.price == rhs.price)||((this.price!= null)&&this.price.equals(rhs.price))));
    }

}
