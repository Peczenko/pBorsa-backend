package com.pborsa.api.repository;

import com.pborsa.api.domain.entity.OrderHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderHistoryRepository extends JpaRepository<OrderHistoryEntity, UUID> {
}
