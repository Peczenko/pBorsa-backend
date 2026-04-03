package com.pborsa.api.strategy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing a base strategy template.
 * These are read-only catalog entries that users can subscribe to.
 */
@Entity
@Table(name = "base_strategies")
@Getter
@Setter
@Accessors(chain = true)
public class BaseStrategyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "base_strategies_id_seq")
    @SequenceGenerator(name = "base_strategies_id_seq", sequenceName = "base_strategies_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 512)
    private String description;

    @Column(nullable = false)
    private Boolean active;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}



