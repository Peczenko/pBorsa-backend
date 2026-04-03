package com.pborsa.api.backtest.entity;

import com.pborsa.api.strategy.entity.BaseStrategyEntity;
import com.pborsa.domain.entity.BacktestStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a backtest execution.
 */
@Entity
@Table(name = "backtests")
@Getter
@Setter
@Accessors(chain = true)
public class BacktestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "backtests_id_seq")
    @SequenceGenerator(name = "backtests_id_seq", sequenceName = "backtests_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_strategy_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_backtests_base_strategy"))
    private BaseStrategyEntity baseStrategy;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 16)
    private String symbol;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal budget;

    @Column(name = "testing_start", nullable = false)
    private Instant testingStart;

    @Column(name = "testing_end", nullable = false)
    private Instant testingEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BacktestStatus status = BacktestStatus.CREATED;

    @Column(precision = 19, scale = 4)
    private BigDecimal pnl;

    @Column(name = "max_drawdown", precision = 19, scale = 4)
    private BigDecimal maxDrawdown;

    @Column(name = "total_trades")
    private Integer totalTrades;

    @Column(name = "winning_trades")
    private Integer winningTrades;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "backtest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BacktestOrderEntity> orders = new ArrayList<>();

    /**
     * Adds an order to this backtest.
     */
    public void addOrder(BacktestOrderEntity order) {
        orders.add(order);
        order.setBacktest(this);
    }
}
