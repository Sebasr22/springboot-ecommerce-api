package com.farmatodo.reto_tecnico.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration.
 * Configures API documentation with security scheme for API Key authentication.
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:reto-tecnico}")
    private String applicationName;

    private static final String SECURITY_SCHEME_NAME = "ApiKeyAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.farmatodo.com")
                                .description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme())
                )
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * API information metadata.
     */
    private Info apiInfo() {
        return new Info()
                .title("Farmatodo Backend API")
                .description("""
                        Backend REST API for Farmatodo e-commerce system.

                        ## Features
                        - Customer management
                        - Product search and inventory
                        - Order creation and processing
                        - Credit card tokenization
                        - Payment processing with retry logic

                        ## Authentication
                        All endpoints under `/api/*` require an API Key in the `X-API-KEY` header.

                        Click the **Authorize** button above to configure your API key.

                        **Default API Key for testing**: `production_api_key_123`
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Farmatodo Development Team")
                        .email("dev@farmatodo.com")
                        .url("https://farmatodo.com")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }

    /**
     * Security scheme definition for API Key authentication.
     * Configures the "Authorize" button in Swagger UI.
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-KEY")
                .description("API Key for authentication. Use: `production_api_key_123` for testing.");
    }
}
