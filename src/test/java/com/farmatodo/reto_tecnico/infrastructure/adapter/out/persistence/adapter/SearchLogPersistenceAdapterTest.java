package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.domain.model.SearchLog;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.SearchLogEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.SearchLogMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.SearchLogJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchLogPersistenceAdapter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchLogPersistenceAdapter Unit Tests")
class SearchLogPersistenceAdapterTest {

    @Mock
    private SearchLogJpaRepository jpaRepository;

    @Mock
    private SearchLogMapper mapper;

    @InjectMocks
    private SearchLogPersistenceAdapter searchLogPersistenceAdapter;

    private SearchLog testSearchLog;
    private SearchLogEntity testSearchLogEntity;

    @BeforeEach
    void setUp() {
        UUID logId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        testSearchLog = SearchLog.builder()
                .id(logId)
                .query("acetaminofen 500mg")
                .resultsCount(15)
                .customerId(customerId)
                .searchTimestamp(now)
                .traceId("trace-12345")
                .build();

        testSearchLogEntity = SearchLogEntity.builder()
                .id(logId)
                .query("acetaminofen 500mg")
                .resultsCount(15)
                .customerId(customerId)
                .searchTimestamp(now)
                .traceId("trace-12345")
                .build();
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save search log successfully")
        void shouldSaveSearchLogSuccessfully() {
            // Given
            when(mapper.toEntity(testSearchLog)).thenReturn(testSearchLogEntity);
            when(jpaRepository.save(testSearchLogEntity)).thenReturn(testSearchLogEntity);
            when(mapper.toDomain(testSearchLogEntity)).thenReturn(testSearchLog);

            // When
            SearchLog result = searchLogPersistenceAdapter.save(testSearchLog);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testSearchLog.getId());
            assertThat(result.getQuery()).isEqualTo("acetaminofen 500mg");
            assertThat(result.getResultsCount()).isEqualTo(15);
            verify(jpaRepository).save(testSearchLogEntity);
        }

        @Test
        @DisplayName("Should save search log with zero results")
        void shouldSaveSearchLogWithZeroResults() {
            // Given
            SearchLog noResultsLog = SearchLog.builder()
                    .id(UUID.randomUUID())
                    .query("nonexistent product")
                    .resultsCount(0)
                    .customerId(null)
                    .searchTimestamp(LocalDateTime.now())
                    .traceId("trace-no-results")
                    .build();

            SearchLogEntity noResultsEntity = SearchLogEntity.builder()
                    .id(noResultsLog.getId())
                    .query("nonexistent product")
                    .resultsCount(0)
                    .customerId(null)
                    .searchTimestamp(noResultsLog.getSearchTimestamp())
                    .traceId("trace-no-results")
                    .build();

            when(mapper.toEntity(noResultsLog)).thenReturn(noResultsEntity);
            when(jpaRepository.save(noResultsEntity)).thenReturn(noResultsEntity);
            when(mapper.toDomain(noResultsEntity)).thenReturn(noResultsLog);

            // When
            SearchLog result = searchLogPersistenceAdapter.save(noResultsLog);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getResultsCount()).isZero();
            assertThat(result.getCustomerId()).isNull();
        }

        @Test
        @DisplayName("Should save search log without customer ID")
        void shouldSaveSearchLogWithoutCustomerId() {
            // Given
            SearchLog anonymousLog = SearchLog.builder()
                    .id(UUID.randomUUID())
                    .query("anonymous search")
                    .resultsCount(5)
                    .customerId(null)
                    .searchTimestamp(LocalDateTime.now())
                    .traceId(null)
                    .build();

            SearchLogEntity anonymousEntity = SearchLogEntity.builder()
                    .id(anonymousLog.getId())
                    .query("anonymous search")
                    .resultsCount(5)
                    .customerId(null)
                    .searchTimestamp(anonymousLog.getSearchTimestamp())
                    .traceId(null)
                    .build();

            when(mapper.toEntity(anonymousLog)).thenReturn(anonymousEntity);
            when(jpaRepository.save(anonymousEntity)).thenReturn(anonymousEntity);
            when(mapper.toDomain(anonymousEntity)).thenReturn(anonymousLog);

            // When
            SearchLog result = searchLogPersistenceAdapter.save(anonymousLog);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isNull();
            assertThat(result.getTraceId()).isNull();
        }

        @Test
        @DisplayName("Should call mapper toEntity and toDomain")
        void shouldCallMapperMethods() {
            // Given
            when(mapper.toEntity(testSearchLog)).thenReturn(testSearchLogEntity);
            when(jpaRepository.save(any(SearchLogEntity.class))).thenReturn(testSearchLogEntity);
            when(mapper.toDomain(testSearchLogEntity)).thenReturn(testSearchLog);

            // When
            searchLogPersistenceAdapter.save(testSearchLog);

            // Then
            verify(mapper).toEntity(testSearchLog);
            verify(mapper).toDomain(testSearchLogEntity);
            verify(jpaRepository).save(testSearchLogEntity);
        }
    }
}
