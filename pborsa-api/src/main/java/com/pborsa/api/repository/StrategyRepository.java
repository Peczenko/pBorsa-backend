package com.pborsa.api.repository;

import com.pborsa.api.domain.entity.StrategyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StrategyRepository extends JpaRepository<StrategyEntity, Long> {
}
