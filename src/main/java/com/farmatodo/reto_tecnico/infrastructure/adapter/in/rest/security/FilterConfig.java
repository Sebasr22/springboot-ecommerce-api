package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.security;

import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.security.filter.TraceIdFilter;
import lombok.RequiredArgsConstructor;
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
 * NOTE: TraceIdFilter is NOT injected because it's not a @Component.
 * It's instantiated directly in the bean method to avoid circular dependency issues.
 */
@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

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
        registrationBean.setFilter(new TraceIdFilter()); // Create instance directly
        registrationBean.addUrlPatterns("/*"); // Apply to ALL requests
        registrationBean.setOrder(0); // HIGHEST priority - executes FIRST
        return registrationBean;
    }

    /**
     * Registers API Key authentication filter.
     * Executes AFTER TraceIdFilter so security logs include trace ID.
     */
    @Bean
    public FilterRegistrationBean<ApiKeyAuthenticationFilter> apiKeyFilter() {
        FilterRegistrationBean<ApiKeyAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(apiKeyAuthenticationFilter);
        registrationBean.addUrlPatterns("/api/*"); // Apply to all /api/** endpoints
        registrationBean.setOrder(1); // Executes after TraceIdFilter
        return registrationBean;
    }
}
