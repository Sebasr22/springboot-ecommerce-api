package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that generates a unique trace ID for each HTTP request.
 * The trace ID is used for request traceability across all logs.
 *
 * Key features:
 * - Generates UUID for each request
 * - Adds trace ID to MDC (Mapped Diagnostic Context) for logging
 * - Includes trace ID in response header (X-Trace-Id)
 * - Clears MDC after request to prevent thread pollution
 *
 * This filter should be registered BEFORE all other filters to ensure
 * that even security logs include the trace ID.
 *
 * NOTE: This class is NOT annotated with @Component.
 * It is registered as a bean in FilterConfig with explicit order (0)
 * to ensure it runs before all other filters.
 */
@Slf4j
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Generate unique trace ID for this request
        String traceId = UUID.randomUUID().toString();

        try {
            // Add trace ID to MDC so it appears in all logs during this request
            MDC.put(TRACE_ID_MDC_KEY, traceId);

            // Add trace ID to response header so client can reference it
            response.setHeader(TRACE_ID_HEADER, traceId);

            log.debug("Request started with trace ID: {}", traceId);

            // Continue filter chain
            filterChain.doFilter(request, response);

            log.debug("Request completed with trace ID: {}", traceId);

        } finally {
            // CRITICAL: Clear MDC to prevent trace ID leaking to other requests
            // on thread pool reuse
            MDC.clear();
        }
    }
}
