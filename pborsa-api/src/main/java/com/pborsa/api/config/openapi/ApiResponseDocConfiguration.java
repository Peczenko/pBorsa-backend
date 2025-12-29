package com.pborsa.api.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocAnnotationsUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Registers ApiResponseDoc annotations as OpenAPI responses.
 */
@Configuration
public class ApiResponseDocConfiguration {

    @Bean
    public OperationCustomizer apiResponseDocCustomizer(OpenAPI openAPI) {
        return (operation, handlerMethod) -> {
            ApiResponseDoc[] docs = handlerMethod.getMethod().getAnnotationsByType(ApiResponseDoc.class);

            if (docs.length == 0) {
                return operation;
            }

            ApiResponses responses = operation.getResponses();

            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            Components components = openAPI.getComponents();

            if (components == null) {
                components = new Components();
                openAPI.setComponents(components);
            }

            SpecVersion specVersion = openAPI.getSpecVersion() != null ? openAPI.getSpecVersion() : SpecVersion.V30;

            for (ApiResponseDoc doc : docs) {
                ApiResponse response = new ApiResponse().description(doc.description());

                if (!Void.class.equals(doc.implementation())) {
                    Schema<?> schema = SpringDocAnnotationsUtils.resolveSchemaFromType(doc.implementation(), components, null, null, specVersion);

                    if (doc.array()) {
                        ArraySchema arraySchema = new ArraySchema();
                        arraySchema.setItems(schema);
                        schema = arraySchema;
                    }

                    MediaType mediaType = new MediaType().schema(schema);
                    response.content(new io.swagger.v3.oas.models.media.Content().addMediaType(doc.mediaType(), mediaType));
                }
                responses.addApiResponse(doc.code(), response);
            }
            return operation;
        };
    }
}

