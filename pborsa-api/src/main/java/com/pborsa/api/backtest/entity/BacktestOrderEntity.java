package com.pborsa.api.backtest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity representing an order executed during a backtest.
 */
@Entity
@Table(name = "backtest_orders")
@Getter
@Setter
@Accessors(chain = true)
public class BacktestOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "backtest_orders_id_seq")
    @SequenceGenerator(name = "backtest_orders_id_seq", sequenceName = "backtest_orders_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backtest_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_backtest_orders_backtest"))
    private BacktestEntity backtest;

    @Column(nullable = false, length = 16)
    private String symbol;

    @Column(nullable = false, length = 8)
    private String side;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    @CreationTimestamp
    private Instant createdAt;
}
