package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TraceIdFilter.
 * Tests trace ID generation and MDC management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TraceIdFilter Unit Tests")
class TraceIdFilterTest {

    private TraceIdFilter traceIdFilter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        traceIdFilter = new TraceIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate trace ID and add to response header")
    void shouldGenerateTraceIdAndAddToResponseHeader() throws ServletException, IOException {
        // Given
        request.setRequestURI("/api/products");

        // When
        traceIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getHeader("X-Trace-Id")).isNotNull();
        assertThat(response.getHeader("X-Trace-Id")).matches("[a-f0-9\\-]{36}"); // UUID format
    }

    @Test
    @DisplayName("Should continue filter chain after setting trace ID")
    void shouldContinueFilterChainAfterSettingTraceId() throws ServletException, IOException {
        // Given
        request.setRequestURI("/api/products");

        // When
        traceIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should clear MDC after request completes")
    void shouldClearMdcAfterRequestCompletes() throws ServletException, IOException {
        // Given
        request.setRequestURI("/api/products");

        // When
        traceIdFilter.doFilterInternal(request, response, filterChain);

        // Then - MDC should be cleared after filter execution
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    @DisplayName("Should generate unique trace IDs for different requests")
    void shouldGenerateUniqueTraceIdsForDifferentRequests() throws ServletException, IOException {
        // Given
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        MockHttpServletRequest request2 = new MockHttpServletRequest();

        // When
        traceIdFilter.doFilterInternal(request1, response1, filterChain);
        traceIdFilter.doFilterInternal(request2, response2, filterChain);

        // Then
        String traceId1 = response1.getHeader("X-Trace-Id");
        String traceId2 = response2.getHeader("X-Trace-Id");
        assertThat(traceId1).isNotEqualTo(traceId2);
    }

    @Test
    @DisplayName("Should handle exception in filter chain and still clear MDC")
    void shouldHandleExceptionInFilterChainAndStillClearMdc() throws ServletException, IOException {
        // Given
        request.setRequestURI("/api/products");
        doThrow(new ServletException("Test exception"))
                .when(filterChain).doFilter(request, response);

        // When & Then
        try {
            traceIdFilter.doFilterInternal(request, response, filterChain);
        } catch (ServletException e) {
            // Expected exception
        }

        // MDC should still be cleared
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    @DisplayName("Should set trace ID in MDC during request processing")
    void shouldSetTraceIdInMdcDuringRequestProcessing() throws ServletException, IOException {
        // Given
        request.setRequestURI("/api/products");
        final String[] capturedTraceId = new String[1];

        doAnswer(invocation -> {
            capturedTraceId[0] = MDC.get("traceId");
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        traceIdFilter.doFilterInternal(request, response, filterChain);

        // Then - MDC had trace ID during filter chain execution
        assertThat(capturedTraceId[0]).isNotNull();
        assertThat(capturedTraceId[0]).matches("[a-f0-9\\-]{36}");
    }
}
