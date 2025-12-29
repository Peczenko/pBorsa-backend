package com.pborsa.api.config.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simple response documentation annotation for OpenAPI customization.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiResponseDocs.class)
public @interface ApiResponseDoc {

    String code();

    String description() default "";

    Class<?> implementation() default Void.class;

    String mediaType() default "application/json";

    boolean array() default false;
}
