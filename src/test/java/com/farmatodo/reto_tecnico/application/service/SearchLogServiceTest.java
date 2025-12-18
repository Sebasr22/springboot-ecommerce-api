package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.model.SearchLog;
import com.farmatodo.reto_tecnico.domain.port.out.SearchLogRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchLogService.
 * Tests async search logging functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchLogService Unit Tests")
class SearchLogServiceTest {

    @Mock
    private SearchLogRepositoryPort searchLogRepository;

    @InjectMocks
    private SearchLogService searchLogService;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should log search async successfully")
    void shouldLogSearchAsyncSuccessfully() {
        // Given
        String query = "acetaminofen";
        int resultsCount = 5;
        SearchLog savedLog = SearchLog.builder()
                .id(UUID.randomUUID())
                .query(query)
                .resultsCount(resultsCount)
                .build();
        when(searchLogRepository.save(any(SearchLog.class))).thenReturn(savedLog);

        // When
        searchLogService.logSearchAsync(query, resultsCount);

        // Then
        ArgumentCaptor<SearchLog> searchLogCaptor = ArgumentCaptor.forClass(SearchLog.class);
        verify(searchLogRepository).save(searchLogCaptor.capture());

        SearchLog capturedLog = searchLogCaptor.getValue();
        assertThat(capturedLog.getQuery()).isEqualTo(query);
        assertThat(capturedLog.getResultsCount()).isEqualTo(resultsCount);
        assertThat(capturedLog.getId()).isNotNull();
        assertThat(capturedLog.getSearchTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should capture traceId from MDC")
    void shouldCaptureTraceIdFromMdc() {
        // Given
        String traceId = "test-trace-id-12345";
        MDC.put("traceId", traceId);
        when(searchLogRepository.save(any(SearchLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        searchLogService.logSearchAsync("test query", 10);

        // Then
        ArgumentCaptor<SearchLog> searchLogCaptor = ArgumentCaptor.forClass(SearchLog.class);
        verify(searchLogRepository).save(searchLogCaptor.capture());

        SearchLog capturedLog = searchLogCaptor.getValue();
        assertThat(capturedLog.getTraceId()).isEqualTo(traceId);
    }

    @Test
    @DisplayName("Should handle null traceId gracefully")
    void shouldHandleNullTraceIdGracefully() {
        // Given
        MDC.remove("traceId");
        when(searchLogRepository.save(any(SearchLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        searchLogService.logSearchAsync("test query", 5);

        // Then
        ArgumentCaptor<SearchLog> searchLogCaptor = ArgumentCaptor.forClass(SearchLog.class);
        verify(searchLogRepository).save(searchLogCaptor.capture());

        SearchLog capturedLog = searchLogCaptor.getValue();
        assertThat(capturedLog.getTraceId()).isNull();
    }

    @Test
    @DisplayName("Should not throw exception when repository fails")
    void shouldNotThrowExceptionWhenRepositoryFails() {
        // Given
        when(searchLogRepository.save(any(SearchLog.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then - should not throw
        assertThatCode(() -> searchLogService.logSearchAsync("failing query", 0))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should set customerId as null")
    void shouldSetCustomerIdAsNull() {
        // Given
        when(searchLogRepository.save(any(SearchLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        searchLogService.logSearchAsync("test", 1);

        // Then
        ArgumentCaptor<SearchLog> searchLogCaptor = ArgumentCaptor.forClass(SearchLog.class);
        verify(searchLogRepository).save(searchLogCaptor.capture());

        assertThat(searchLogCaptor.getValue().getCustomerId()).isNull();
    }

    @Test
    @DisplayName("Should handle empty query string")
    void shouldHandleEmptyQueryString() {
        // Given
        when(searchLogRepository.save(any(SearchLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        searchLogService.logSearchAsync("", 0);

        // Then
        ArgumentCaptor<SearchLog> searchLogCaptor = ArgumentCaptor.forClass(SearchLog.class);
        verify(searchLogRepository).save(searchLogCaptor.capture());

        assertThat(searchLogCaptor.getValue().getQuery()).isEmpty();
    }

    @Test
    @DisplayName("Should handle zero results count")
    void shouldHandleZeroResultsCount() {
        // Given
        when(searchLogRepository.save(any(SearchLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        searchLogService.logSearchAsync("no-results-query", 0);

        // Then
        ArgumentCaptor<SearchLog> searchLogCaptor = ArgumentCaptor.forClass(SearchLog.class);
        verify(searchLogRepository).save(searchLogCaptor.capture());

        assertThat(searchLogCaptor.getValue().getResultsCount()).isZero();
    }

    @Test
    @DisplayName("Should generate unique ID for each search log")
    void shouldGenerateUniqueIdForEachSearchLog() {
        // Given
        when(searchLogRepository.save(any(SearchLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        searchLogService.logSearchAsync("query1", 1);
        searchLogService.logSearchAsync("query2", 2);

        // Then
        ArgumentCaptor<SearchLog> searchLogCaptor = ArgumentCaptor.forClass(SearchLog.class);
        verify(searchLogRepository, times(2)).save(searchLogCaptor.capture());

        UUID id1 = searchLogCaptor.getAllValues().get(0).getId();
        UUID id2 = searchLogCaptor.getAllValues().get(1).getId();

        assertThat(id1).isNotEqualTo(id2);
    }
}
