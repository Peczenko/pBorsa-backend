package com.pborsa.worker.config.temporal;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(TemporalEnabledCondition.class)
public @interface ConditionalOnTemporalEnabled {
}

