package com.pborsa.api.order.entity;

import com.pborsa.domain.dto.trading.OrderStatus;
import com.pborsa.domain.dto.trading.OrderStatusReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_history")
@Getter
@Setter
@Accessors(chain = true)
public class OrderHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_history_order"))
    private OrderEntity order;
    
    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private OrderStatusReason reason;

    @Column(length = 512)
    private String message;

    @CreationTimestamp
    private Instant createdAt;

    public UUID getOrderId() {
        return order != null ? order.getId() : null;
    }

    public OrderHistoryEntity setOrderId(UUID orderId) {
        this.order = new OrderEntity().setId(orderId);
        return this;
    }
}
