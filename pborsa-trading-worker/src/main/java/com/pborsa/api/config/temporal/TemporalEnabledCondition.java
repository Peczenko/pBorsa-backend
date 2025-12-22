package com.pborsa.api.config.temporal;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition class that checks if Temporal is enabled.
 * Equivalent to ImperioEnabledCondition in reference project.
 */
public class TemporalEnabledCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment().getProperty("temporal.enabled", "true");
        boolean isEnabled = Boolean.parseBoolean(enabled);
        
        return new ConditionOutcome(
                isEnabled,
                isEnabled 
                        ? "Temporal is enabled" 
                        : "Temporal is disabled"
        );
    }
}

