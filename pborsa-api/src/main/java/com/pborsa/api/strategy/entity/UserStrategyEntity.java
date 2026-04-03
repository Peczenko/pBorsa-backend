package com.pborsa.api.strategy.entity;

import com.pborsa.domain.entity.UserStrategyStatus;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity representing a user's subscription to a base strategy with a specific stock.
 * Each user can subscribe to the same base strategy multiple times with different symbols.
 */
@Entity
@Table(name = "user_strategies", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_strategies_user_base_symbol_status",
                columnNames = {"user_id", "base_strategy_id", "symbol", "status"})
})
@Getter
@Setter
@Accessors(chain = true)
public class UserStrategyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_strategies_id_seq")
    @SequenceGenerator(name = "user_strategies_id_seq", sequenceName = "user_strategies_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_strategy_id", nullable = false, 
            foreignKey = @ForeignKey(name = "fk_user_strategies_base_strategy"))
    private BaseStrategyEntity baseStrategy;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 16)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserStrategyStatus status = UserStrategyStatus.ACTIVE;

    @Column(precision = 19, scale = 4)
    private BigDecimal budget;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}



