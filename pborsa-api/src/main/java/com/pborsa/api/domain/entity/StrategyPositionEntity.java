package com.pborsa.api.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Entity tracking the current position and P/L for a user strategy.
 * Uses average cost method for P/L calculation.
 */
@Entity
@Table(name = "strategy_positions")
@Getter
@Setter
@Accessors(chain = true)
public class StrategyPositionEntity {

    @Id
    private Long userStrategyId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_strategy_id", foreignKey = @ForeignKey(name = "fk_strategy_positions_user_strategy"))
    private UserStrategyEntity userStrategy;

    /**
     * Current number of shares held (long position).
     */
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal totalShares = BigDecimal.ZERO;

    /**
     * Total cost basis of currently held shares.
     * Average cost per share = totalCostBasis / totalShares
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalCostBasis = BigDecimal.ZERO;

    /**
     * Cumulative realized P/L from closed positions.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal realizedPnL = BigDecimal.ZERO;

    /**
     * Version for optimistic locking.
     */
    @Version
    private Long version;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    /**
     * Calculates the average cost per share.
     *
     * @return Average cost per share, or ZERO if no shares held
     */
    public BigDecimal getAverageCostPerShare() {
        if (totalShares == null || totalShares.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalCostBasis.divide(totalShares, 4, RoundingMode.HALF_UP);
    }

    /**
     * Applies a BUY fill to the position.
     *
     * @param quantity  Number of shares bought
     * @param fillPrice Average fill price
     */
    public void applyBuy(BigDecimal quantity, BigDecimal fillPrice) {
        if (quantity == null || fillPrice == null) {
            return;
        }
        BigDecimal cost = quantity.multiply(fillPrice);
        this.totalShares = this.totalShares.add(quantity);
        this.totalCostBasis = this.totalCostBasis.add(cost);
    }

    /**
     * Applies a SELL fill to the position.
     * Calculates and records realized P/L using average cost method.
     *
     * @param quantity  Number of shares sold
     * @param fillPrice Average fill price
     * @return Realized P/L from this sale
     */
    public BigDecimal applySell(BigDecimal quantity, BigDecimal fillPrice) {
        if (quantity == null || fillPrice == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal avgCost = getAverageCostPerShare();
        
        // Realized P/L = (fillPrice - avgCost) * quantity
        BigDecimal pnl = fillPrice.subtract(avgCost).multiply(quantity);
        
        // Reduce cost basis proportionally
        BigDecimal costReduction = avgCost.multiply(quantity);
        this.totalCostBasis = this.totalCostBasis.subtract(costReduction);
        this.totalShares = this.totalShares.subtract(quantity);
        this.realizedPnL = this.realizedPnL.add(pnl);

        // Ensure no negative values due to rounding
        if (this.totalShares.compareTo(BigDecimal.ZERO) < 0) {
            this.totalShares = BigDecimal.ZERO;
        }
        if (this.totalCostBasis.compareTo(BigDecimal.ZERO) < 0) {
            this.totalCostBasis = BigDecimal.ZERO;
        }

        return pnl;
    }

    /**
     * Calculates unrealized P/L based on current market price.
     *
     * @param currentPrice Current market price per share
     * @return Unrealized P/L
     */
    public BigDecimal calculateUnrealizedPnL(BigDecimal currentPrice) {
        if (currentPrice == null || totalShares.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal avgCost = getAverageCostPerShare();
        return currentPrice.subtract(avgCost).multiply(totalShares);
    }
}

