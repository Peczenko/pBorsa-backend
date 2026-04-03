package com.pborsa.api.credentials;

import com.pborsa.api.strategy.repository.UserStrategyRepository;
import com.pborsa.trading.credentials.ActiveStrategyChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ActiveStrategyChecker that checks the user strategies repository.
 */
@Service
@RequiredArgsConstructor
public class ActiveStrategyCheckerImpl implements ActiveStrategyChecker {

    private final UserStrategyRepository userStrategyRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveStrategies(Long userId) {
        if (userId == null) {
            return false;
        }
        return userStrategyRepository.hasActiveOrPreparingStrategies(userId);
    }
}
