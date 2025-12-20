package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApiKeyAuthenticationFilter.
 * Tests API key validation and whitelisted paths.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApiKeyAuthenticationFilter Unit Tests")
class ApiKeyAuthenticationFilterTest {

    private ApiKeyAuthenticationFilter filter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String VALID_API_KEY = "test-api-key-12345";
    private static final String API_KEY_HEADER = "X-API-KEY";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        filter = new ApiKeyAuthenticationFilter(objectMapper, VALID_API_KEY);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("Whitelisted Paths Tests")
    class WhitelistedPathsTests {

        @Test
        @DisplayName("Should allow /ping without API key")
        void shouldAllowPingWithoutApiKey() throws ServletException, IOException {
            // Given
            request.setRequestURI("/ping");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        @Test
        @DisplayName("Should allow /actuator/* without API key")
        void shouldAllowActuatorWithoutApiKey() throws ServletException, IOException {
            // Given
            request.setRequestURI("/actuator/health");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should allow /swagger-ui/* without API key")
        void shouldAllowSwaggerUiWithoutApiKey() throws ServletException, IOException {
            // Given
            request.setRequestURI("/swagger-ui/index.html");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should allow /v3/api-docs/* without API key")
        void shouldAllowApiDocsWithoutApiKey() throws ServletException, IOException {
            // Given
            request.setRequestURI("/v3/api-docs/swagger-config");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should allow /swagger-ui.html without API key")
        void shouldAllowSwaggerUiHtmlWithoutApiKey() throws ServletException, IOException {
            // Given
            request.setRequestURI("/swagger-ui.html");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("API Key Validation Tests")
    class ApiKeyValidationTests {

        @Test
        @DisplayName("Should allow request with valid API key")
        void shouldAllowRequestWithValidApiKey() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/products");
            request.addHeader(API_KEY_HEADER, VALID_API_KEY);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        @Test
        @DisplayName("Should reject request without API key")
        void shouldRejectRequestWithoutApiKey() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/products");
            request.setMethod("GET");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, never()).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.getContentAsString()).contains("API key is required");
        }

        @Test
        @DisplayName("Should reject request with blank API key")
        void shouldRejectRequestWithBlankApiKey() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/products");
            request.setMethod("GET");
            request.addHeader(API_KEY_HEADER, "   ");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, never()).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("Should reject request with invalid API key")
        void shouldRejectRequestWithInvalidApiKey() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/products");
            request.setMethod("GET");
            request.addHeader(API_KEY_HEADER, "wrong-api-key");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, never()).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.getContentAsString()).contains("Invalid API key");
        }
    }

    @Nested
    @DisplayName("Error Response Tests")
    class ErrorResponseTests {

        @Test
        @DisplayName("Should return JSON error response")
        void shouldReturnJsonErrorResponse() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/products");
            request.setMethod("POST");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getContentAsString()).contains("\"status\":401");
            assertThat(response.getContentAsString()).contains("\"error\":\"UNAUTHORIZED\"");
        }

        @Test
        @DisplayName("Should include hint in error response")
        void shouldIncludeHintInErrorResponse() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/products");
            request.setMethod("GET");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(response.getContentAsString()).contains("X-API-KEY");
        }

        @Test
        @DisplayName("Should include timestamp in error response")
        void shouldIncludeTimestampInErrorResponse() throws ServletException, IOException {
            // Given
            request.setRequestURI("/api/orders");
            request.setMethod("GET");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(response.getContentAsString()).contains("timestamp");
        }
    }
}
