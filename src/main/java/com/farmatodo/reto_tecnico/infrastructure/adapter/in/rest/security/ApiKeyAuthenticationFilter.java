package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * API Key authentication filter.
 * Validates X-API-KEY header for all requests except whitelisted paths.
 *
 * Security:
 * - Returns 401 Unauthorized if API key is missing or invalid
 * - Whitelisted paths: /ping, /actuator/*, /swagger-ui/*, /v3/api-docs/*
 * - API key should be configured via environment variable in production
 *
 * NOTE: This filter is NOT a @Component. It is instantiated manually
 * in FilterConfig to avoid double registration issues.
 */
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final ObjectMapper objectMapper;
    private final String configuredApiKey;

    /**
     * Constructor for manual instantiation in FilterConfig.
     * @param objectMapper JSON serializer for error responses
     * @param configuredApiKey the API key to validate against
     */
    public ApiKeyAuthenticationFilter(ObjectMapper objectMapper, String configuredApiKey) {
        this.objectMapper = objectMapper;
        this.configuredApiKey = configuredApiKey;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Skip authentication for whitelisted paths
        if (isWhitelisted(requestPath)) {
            log.debug("Whitelisted path accessed: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // Extract API key from header
        String providedApiKey = request.getHeader(API_KEY_HEADER);

        if (providedApiKey == null || providedApiKey.isBlank()) {
            log.warn("Missing API key for request: {} {}", request.getMethod(), requestPath);
            sendUnauthorizedResponse(response, "API key is required. Provide X-API-KEY header.");
            return;
        }

        // Validate API key
        if (!configuredApiKey.equals(providedApiKey)) {
            log.warn("Invalid API key attempted for request: {} {}", request.getMethod(), requestPath);
            sendUnauthorizedResponse(response, "Invalid API key");
            return;
        }

        log.debug("API key validated successfully for: {} {}", request.getMethod(), requestPath);
        filterChain.doFilter(request, response);
    }

    /**
     * Checks if the request path should bypass API key authentication.
     * @param path request URI path
     * @return true if path is whitelisted
     */
    private boolean isWhitelisted(String path) {
        return path.equals("/ping") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/swagger-ui.html");
    }

    /**
     * Sends 401 Unauthorized response with error details.
     * @param response HTTP response
     * @param message error message
     * @throws IOException if writing response fails
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.UNAUTHORIZED.value());
        errorBody.put("error", "UNAUTHORIZED");
        errorBody.put("message", message);
        errorBody.put("hint", "Add 'X-API-KEY' header with valid API key");

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
