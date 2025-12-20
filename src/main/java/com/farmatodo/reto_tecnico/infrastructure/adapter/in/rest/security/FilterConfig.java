package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.security.filter.TraceIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filter configuration for request processing.
 * Configures the execution order of filters:
 * 1. TraceIdFilter (order=0) - Generates trace ID for request traceability
 * 2. ApiKeyAuthenticationFilter (order=1) - Validates API Key
 *
 * IMPORTANT: TraceIdFilter must execute FIRST to ensure all logs
 * (including security logs) contain the trace ID.
 *
 * NOTE: Both filters are instantiated directly (not @Component) to avoid
 * double registration issues with Spring Boot's auto-configuration.
 */
@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final ObjectMapper objectMapper;

    @Value("${farmatodo.api.key:default-dev-key-change-in-production}")
    private String apiKey;

    /**
     * Registers TraceIdFilter with highest priority.
     * Executes BEFORE all other filters to ensure trace ID is available
     * in all subsequent logs (including security, CORS, etc.).
     *
     * NOTE: Creates new instance directly (not injected) to avoid
     * bean definition override conflicts.
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TraceIdFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0);
        return registrationBean;
    }

    /**
     * Registers API Key authentication filter.
     * Executes AFTER TraceIdFilter so security logs include trace ID.
     *
     * NOTE: Creates new instance directly, passing ObjectMapper and API key
     * to avoid double registration issues.
     */
    @Bean
    public FilterRegistrationBean<ApiKeyAuthenticationFilter> apiKeyFilter() {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(objectMapper, apiKey);

        FilterRegistrationBean<ApiKeyAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
