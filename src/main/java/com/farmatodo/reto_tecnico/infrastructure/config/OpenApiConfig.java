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
                                .url("https://ft-api.srodriguez-tech.com")
                                .description("Production Server (Live Demo)"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
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
                .title("Farmatodo Challenge API")
                .description("""
                        Backend REST API developed for the Farmatodo Senior Backend Developer Challenge.
                        
                        This API implements a complete e-commerce flow using Hexagonal Architecture, 
                        SOLID principles, and robust error handling.

                        ## Key Features
                        - **Customer Management**: Registration and validation.
                        - **Inventory**: Product search with pagination.
                        - **Shopping Cart**: Add, remove, and checkout items.
                        - **Secure Payments**: Credit card tokenization simulation.
                        - **Resilience**: Retry logic and transaction management.

                        ## Authentication
                        This API is secured. You need an `X-API-KEY` to access the endpoints.
                        Please use the **Authorize** button at the top right to enter your credentials.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Sebastian Rodriguez") // Tu nombre
                        .url("https://www.linkedin.com/in/sebastian-rodriguez-9340a2191/")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }

    /**
     * Security scheme definition for API Key authentication.
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-KEY")
                .description("Enter the API Key provided for the challenge review.");
    }
}