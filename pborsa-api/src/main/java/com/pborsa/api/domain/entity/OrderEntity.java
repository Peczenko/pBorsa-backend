package com.pborsa.api.domain.entity;

import com.pborsa.api.domain.dto.trading.OrderSide;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderType;
import com.pborsa.api.domain.dto.trading.TimeInForce;
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
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Accessors(chain = true)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id", foreignKey = @ForeignKey(name = "fk_orders_strategy"))
    private StrategyEntity strategy;

    private String workflowId;
    private String alpacaOrderId;
    private String clientOrderId;
    private String symbol;

    @Enumerated(EnumType.STRING)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    private TimeInForce timeInForce;

    private BigDecimal quantity;
    private BigDecimal filledQuantity;
    private BigDecimal limitPrice;
    private BigDecimal stopPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Boolean extendedHours;

    private Instant submittedAt;
    private Instant filledAt;
    private Instant expiredAt;
    private Instant cancelledAt;
    private Instant createdAtRemote;
    private Instant updatedAtRemote;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
