package com.pborsa.api.config.temporal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * Conditional annotation for enabling beans only when Temporal is enabled.
 * Equivalent to ConditionalOnImperioEnabled in reference project.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(TemporalEnabledCondition.class)
public @interface ConditionalOnTemporalEnabled {
}

