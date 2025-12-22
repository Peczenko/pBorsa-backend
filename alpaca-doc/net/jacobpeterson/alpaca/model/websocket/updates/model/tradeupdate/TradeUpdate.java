
package net.jacobpeterson.alpaca.model.websocket.updates.model.tradeupdate;

import java.io.Serializable;
import java.time.OffsetDateTime;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.openapi.trader.model.Order;

public class TradeUpdate implements Serializable
{

    /**
     * The {@link net.jacobpeterson.alpaca.model.websocket.updates.model.tradeupdate.TradeUpdateEvent}.
     * <p>
     * 
     * 
     */
    @SerializedName("event")
    @Expose
    private TradeUpdateEvent event;
    /**
     * The execution ID.
     * <p>
     * 
     * 
     */
    @SerializedName("execution_id")
    @Expose
    private String executionId;
    /**
     * The price.
     * <p>
     * 
     * 
     */
    @SerializedName("price")
    @Expose
    private String price;
    /**
     * The position quantity.
     * <p>
     * 
     * Corresponds to the "position_qty" property.
     * 
     */
    @SerializedName("position_qty")
    @Expose
    private String positionQuantity;
    /**
     * The quantity.
     * <p>
     * 
     * Corresponds to the "qty" property.
     * 
     */
    @SerializedName("qty")
    @Expose
    private String quantity;
    /**
     * The timestamp.
     * <p>
     * 
     * 
     */
    @SerializedName("timestamp")
    @Expose
    private OffsetDateTime timestamp;
    /**
     * The {@link net.jacobpeterson.alpaca.openapi.trader.model.Order}.
     * <p>
     * 
     * 
     */
    @SerializedName("order")
    @Expose
    private Order order;
    private final static long serialVersionUID = 7553458129647159041L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public TradeUpdate() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public TradeUpdate(TradeUpdate source) {
        super();
        this.event = source.event;
        this.executionId = source.executionId;
        this.price = source.price;
        this.positionQuantity = source.positionQuantity;
        this.quantity = source.quantity;
        this.timestamp = source.timestamp;
        this.order = source.order;
    }

    /**
     * 
     * @param executionId
     *     The execution ID.
     * @param quantity
     *     The quantity.
     * @param price
     *     The price.
     * @param positionQuantity
     *     The position quantity.
     * @param event
     *     The {@link net.jacobpeterson.alpaca.model.websocket.updates.model.tradeupdate.TradeUpdateEvent}.
     * @param timestamp
     *     The timestamp.
     * @param order
     *     The {@link net.jacobpeterson.alpaca.openapi.trader.model.Order}.
     */
    public TradeUpdate(TradeUpdateEvent event, String executionId, String price, String positionQuantity, String quantity, OffsetDateTime timestamp, Order order) {
        super();
        this.event = event;
        this.executionId = executionId;
        this.price = price;
        this.positionQuantity = positionQuantity;
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.order = order;
    }

    /**
     * The {@link net.jacobpeterson.alpaca.model.websocket.updates.model.tradeupdate.TradeUpdateEvent}.
     * <p>
     * 
     * 
     */
    public TradeUpdateEvent getEvent() {
        return event;
    }

    /**
     * The {@link net.jacobpeterson.alpaca.model.websocket.updates.model.tradeupdate.TradeUpdateEvent}.
     * <p>
     * 
     * 
     */
    public void setEvent(TradeUpdateEvent event) {
        this.event = event;
    }

    public TradeUpdate withEvent(TradeUpdateEvent event) {
        this.event = event;
        return this;
    }

    /**
     * The execution ID.
     * <p>
     * 
     * 
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * The execution ID.
     * <p>
     * 
     * 
     */
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public TradeUpdate withExecutionId(String executionId) {
        this.executionId = executionId;
        return this;
    }

    /**
     * The price.
     * <p>
     * 
     * 
     */
    public String getPrice() {
        return price;
    }

    /**
     * The price.
     * <p>
     * 
     * 
     */
    public void setPrice(String price) {
        this.price = price;
    }

    public TradeUpdate withPrice(String price) {
        this.price = price;
        return this;
    }

    /**
     * The position quantity.
     * <p>
     * 
     * Corresponds to the "position_qty" property.
     * 
     */
    public String getPositionQuantity() {
        return positionQuantity;
    }

    /**
     * The position quantity.
     * <p>
     * 
     * Corresponds to the "position_qty" property.
     * 
     */
    public void setPositionQuantity(String positionQuantity) {
        this.positionQuantity = positionQuantity;
    }

    public TradeUpdate withPositionQuantity(String positionQuantity) {
        this.positionQuantity = positionQuantity;
        return this;
    }

    /**
     * The quantity.
     * <p>
     * 
     * Corresponds to the "qty" property.
     * 
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * The quantity.
     * <p>
     * 
     * Corresponds to the "qty" property.
     * 
     */
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public TradeUpdate withQuantity(String quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * The timestamp.
     * <p>
     * 
     * 
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * The timestamp.
     * <p>
     * 
     * 
     */
    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TradeUpdate withTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * The {@link net.jacobpeterson.alpaca.openapi.trader.model.Order}.
     * <p>
     * 
     * 
     */
    public Order getOrder() {
        return order;
    }

    /**
     * The {@link net.jacobpeterson.alpaca.openapi.trader.model.Order}.
     * <p>
     * 
     * 
     */
    public void setOrder(Order order) {
        this.order = order;
    }

    public TradeUpdate withOrder(Order order) {
        this.order = order;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TradeUpdate.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("event");
        sb.append('=');
        sb.append(((this.event == null)?"<null>":this.event));
        sb.append(',');
        sb.append("executionId");
        sb.append('=');
        sb.append(((this.executionId == null)?"<null>":this.executionId));
        sb.append(',');
        sb.append("price");
        sb.append('=');
        sb.append(((this.price == null)?"<null>":this.price));
        sb.append(',');
        sb.append("positionQuantity");
        sb.append('=');
        sb.append(((this.positionQuantity == null)?"<null>":this.positionQuantity));
        sb.append(',');
        sb.append("quantity");
        sb.append('=');
        sb.append(((this.quantity == null)?"<null>":this.quantity));
        sb.append(',');
        sb.append("timestamp");
        sb.append('=');
        sb.append(((this.timestamp == null)?"<null>":this.timestamp));
        sb.append(',');
        sb.append("order");
        sb.append('=');
        sb.append(((this.order == null)?"<null>":this.order));
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
        result = ((result* 31)+((this.executionId == null)? 0 :this.executionId.hashCode()));
        result = ((result* 31)+((this.quantity == null)? 0 :this.quantity.hashCode()));
        result = ((result* 31)+((this.price == null)? 0 :this.price.hashCode()));
        result = ((result* 31)+((this.positionQuantity == null)? 0 :this.positionQuantity.hashCode()));
        result = ((result* 31)+((this.event == null)? 0 :this.event.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        result = ((result* 31)+((this.order == null)? 0 :this.order.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TradeUpdate) == false) {
            return false;
        }
        TradeUpdate rhs = ((TradeUpdate) other);
        return ((((((((this.executionId == rhs.executionId)||((this.executionId!= null)&&this.executionId.equals(rhs.executionId)))&&((this.quantity == rhs.quantity)||((this.quantity!= null)&&this.quantity.equals(rhs.quantity))))&&((this.price == rhs.price)||((this.price!= null)&&this.price.equals(rhs.price))))&&((this.positionQuantity == rhs.positionQuantity)||((this.positionQuantity!= null)&&this.positionQuantity.equals(rhs.positionQuantity))))&&((this.event == rhs.event)||((this.event!= null)&&this.event.equals(rhs.event))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))))&&((this.order == rhs.order)||((this.order!= null)&&this.order.equals(rhs.order))));
    }

}
