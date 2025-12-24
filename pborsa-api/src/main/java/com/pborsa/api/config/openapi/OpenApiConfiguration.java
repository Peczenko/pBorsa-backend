package com.pborsa.api.config.openapi;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for the pBorsa API.
 */
@Configuration
@ConditionalOnClass(name = "io.swagger.v3.oas.models.OpenAPI")
public class OpenApiConfiguration {

    @Bean
    public OpenAPI pborsaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("pBorsa API")
                        .description("Trading support API (accounts, credentials, strategies, data streaming)")
                        .version("v1")
                        .contact(new Contact().name("pBorsa Team")))
                .externalDocs(new ExternalDocumentation()
                        .description("Swagger UI")
                        .url("/swagger-ui/index.html"));
    }
}
