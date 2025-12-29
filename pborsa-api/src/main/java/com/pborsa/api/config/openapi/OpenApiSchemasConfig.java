package com.pborsa.api.config.openapi;

import com.pborsa.api.config.openapi.model.ApiErrorResponseDoc;
import com.pborsa.api.config.openapi.model.ApiErrorResponseDocBase;
import com.pborsa.api.config.openapi.model.ApiSuccessResponseDocBase;
import com.pborsa.api.config.openapi.model.UserProfileResponseDoc;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class OpenApiSchemasConfig {

    @Bean
    public OpenApiCustomizer registerResponseDocSchemas() {
        return openApi -> {
            Components components = Optional.ofNullable(openApi.getComponents())
                    .orElseGet(() -> {
                        Components c = new Components();
                        openApi.setComponents(c);
                        return c;
                    });

            register(components, UserProfileResponseDoc.class);
            register(components, ApiErrorResponseDoc.class);
            register(components, ApiSuccessResponseDocBase.class);
            register(components, ApiErrorResponseDocBase.class);
        };
    }

    private void register(Components components, Class<?> clazz) {
        String schemaName = Optional.ofNullable(clazz.getAnnotation(Schema.class))
                .map(Schema::name)
                .filter(n -> !n.isBlank())
                .orElse(clazz.getSimpleName());

        ResolvedSchema resolved = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(clazz).resolveAsRef(false));

        components.addSchemas(schemaName, resolved.schema);
        if (resolved.referencedSchemas != null) {
            resolved.referencedSchemas.forEach(components::addSchemas);
        }
    }
}
